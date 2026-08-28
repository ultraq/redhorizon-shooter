/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nz.net.ultraq.redhorizon.shooter

import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.physics.CircleCollider
import nz.net.ultraq.redhorizon.physics.CollisionStartEvent
import nz.net.ultraq.redhorizon.runtime.objects.ScreenEdges
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.RESOURCE_MANAGER

import org.joml.Matrix4fc
import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * A single bullet fired from the player ship.
 *
 * @author Emanuel Rabina
 */
class Bullet extends Node<Bullet> {

	static float bulletSpeed = 600f
	static float bulletLifetime = 1f

	// TODO: These should come from the object data for the bullet sprite
	final int headings = 32
	final float headingStep = 360f / headings as float

	private final float heading
	private final Vector2f initialVelocity
	private final Vector2f vector

	/**
	 * Constructor, create a new bullet at the given position and with an initial
	 * velocity as supplied by the object firing it.
	 */
	Bullet(Matrix4fc initialTransform, float heading, Vector2f initialVelocity) {

		setTransform(initialTransform)
		this.heading = heading
		this.initialVelocity = initialVelocity
		vector = new Vector2f(Math.sin(Math.toRadians(heading)) as float, Math.cos(Math.toRadians(heading)) as float)

		var resourceManager = RESOURCE_MANAGER.get()

		// NOTE: C&C unit headings were ordered in a counter-clockwise order, the
		//       reverse from how degrees-based headings are done.
		var closestHeading = Math.round(heading / headingStep)
		var frame = closestHeading ? headings - closestHeading as int : 0
		var bulletSpriteSheet = resourceManager.loadSpriteSheet('tracer.shp')
		addChild(new Sprite(bulletSpriteSheet, PalettedSpriteShader)
			.withFramePosition(frame))

		addChild(new CircleCollider(2f))
		addChild(new ScriptNode(BulletScript))
	}

	/**
	 * Bullet behaviour script.
	 */
	static class BulletScript extends Script<Bullet> {

		private static final Logger logger = LoggerFactory.getLogger(BulletScript)

		private float bulletTimer
		private boolean queuedForRemoval = false

		@Override
		void init() {

			node.find(CircleCollider).on(CollisionStartEvent) { event ->
				var otherObject = event.otherCollider().parent

				if (otherObject instanceof ScreenEdges && !queuedForRemoval) {
					logger.debug('Bullet collided with {} - removing from scene', otherObject.name)
					node.scene.queueUpdate { ->
						node.parent?.removeChild(node)
						node.close()
					}
					queuedForRemoval = true
				}
			}
		}

		@Override
		void update(float delta) {

			bulletTimer += delta

			// Destroy bullet if it reaches the max lifetime
			if (bulletTimer > bulletLifetime && !queuedForRemoval) {
				node.scene.queueUpdate { ->
					node.parent?.removeChild(node)
					node.close()
				}
				queuedForRemoval = true
			}

			// Keep moving along
			else {
				node.translate(node.vector.x * bulletSpeed * delta as float, node.vector.y * bulletSpeed * delta as float)
			}
		}
	}
}
