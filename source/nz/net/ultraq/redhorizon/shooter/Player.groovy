/*
 * Copyright 2025, Emanuel Rabina (http://www.ultraq.net.nz/)
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

import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.engine.scripts.Script
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.PaletteSwapMap
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.physics.BoxCollider
import nz.net.ultraq.redhorizon.physics.CollisionEndEvent
import nz.net.ultraq.redhorizon.physics.CollisionStartEvent
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.runtime.objects.ScreenEdges
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.RESOURCE_MANAGER

import org.joml.Vector2f
import org.joml.Vector3f
import static org.lwjgl.glfw.GLFW.*

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> {

	// TODO: These should come from the unit data for the orca sprite
	final int headings = 32
	final float headingStep = 360f / headings as float

	// Player properties adjustable by scripts
	// TODO: These can be moved into their own components if other objects need them
	boolean flying = true
	float heading = 25f
	boolean accelerating = false
	final Vector2f velocity = new Vector2f()

	/**
	 * Constructor, create a new player object.
	 */
	Player() {

		var resourceManager = RESOURCE_MANAGER.get()

		addChild(new PaletteSwapMap(Faction.GOLD.colours)
			.withName('Faction - Gold'))

		var orcaSpriteSheet = resourceManager.loadSpriteSheet('orca.shp')
		addChild(new Sprite(orcaSpriteSheet, PalettedSpriteShader)
			.translate(0f, 24f, 0f)
			.withName('Orca'))
		addChild(new Sprite(orcaSpriteSheet, ShadowShader)
			.withName('Shadow'))
		addChild(new MovementNode(200f))
		addChild(new BoxCollider(24f, 24f))

		addChild(new ScriptNode(PlayerScript))
	}

	/**
	 * A script for controlling the player sprite's bobbing motion.
	 */
	static class PlayerScript extends Script<Player> {

		// TODO: Make these items into variables that can be controlled by ImGui?
		public static final float MAX_SPEED = 400f
		private static final Vector2f up = new Vector2f(0, 1)

		// Bobbing
		private float bobbingTimer = 0f

		// Heading/rotation
		private Vector3f unprojectResult = new Vector3f()
		private Vector2f worldCursorPosition = new Vector2f()
		private Vector2f positionXY = new Vector2f()
		private Vector2f headingToCursor = new Vector2f()

		// Movement
		private Vector2f vector = new Vector2f()
		private boolean hitLeftScreenEdge = false
		private boolean hitRightScreenEdge = false
		private boolean hitTopScreenEdge = false
		private boolean hitBottomScreenEdge = false

		@Override
		void init() {

			node.find(BoxCollider)
				.on(CollisionStartEvent) { event ->
					var otherCollider = event.otherCollider()
					var otherObject = otherCollider.parent
					if (otherObject instanceof ScreenEdges) {
						switch (otherCollider.name) {
							case ScreenEdges.TOP_COLLIDER_NAME:
								hitTopScreenEdge = true
								break
							case ScreenEdges.BOTTOM_COLLIDER_NAME:
								hitBottomScreenEdge = true
								break
							case ScreenEdges.LEFT_COLLIDER_NAME:
								hitLeftScreenEdge = true
								break
							case ScreenEdges.RIGHT_COLLIDER_NAME:
								hitRightScreenEdge = true
								break
						}
					}
				}
				.on(CollisionEndEvent) { event ->
					var otherCollider = event.otherCollider()
					var otherObject = otherCollider.parent
					if (otherObject instanceof ScreenEdges) {
						switch (otherCollider.name) {
							case ScreenEdges.TOP_COLLIDER_NAME:
								hitTopScreenEdge = false
								break
							case ScreenEdges.BOTTOM_COLLIDER_NAME:
								hitBottomScreenEdge = false
								break
							case ScreenEdges.LEFT_COLLIDER_NAME:
								hitLeftScreenEdge = false
								break
							case ScreenEdges.RIGHT_COLLIDER_NAME:
								hitRightScreenEdge = false
								break
						}
					}
				}
		}

		@Override
		void update(float delta) {

			updateBobbing(delta)
			updateHeading()
			updateMovement(delta)
			updateFramePosition()
		}

		/**
		 * Adjust player position to simulate an aircraft bobbing up and down.
		 */
		private void updateBobbing(float delta) {

			if (node.flying) {
				bobbingTimer += delta
				var orcaSprite = node.find('Orca') as Sprite
				var position = orcaSprite.position
				orcaSprite.setPosition(position.x(), 24f + (Math.sin(bobbingTimer) * 8) as float, position.z())
			}
		}

		/**
		 * Adjust the selected sprite frame in each of the player's sprite components.
		 */
		private void updateFramePosition() {

			// TODO: This should be handled by a sprite animation system and node

			// NOTE: C&C unit headings were ordered in a counter-clockwise order, the
			//       reverse from how degrees-based headings are done.
			var closestHeading = Math.round(node.heading / node.headingStep)
			var frame = closestHeading ? node.headings - closestHeading as int : 0
			if (node.accelerating) {
				frame += node.headings
			}

			node.findAll(Sprite).each { sprite ->
				sprite.withFramePosition(sprite.spriteSheet.getFramePosition(frame))
			}
		}

		/**
		 * Update player heading and sprite to always face the cursor.
		 */
		private void updateHeading() {

			var cursorPosition = input.cursorPosition()
			if (cursorPosition) {
				var camera = node.scene.find(Camera)
				positionXY.set(node.position)
				worldCursorPosition.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
				worldCursorPosition.sub(positionXY, headingToCursor)
				node.heading = Math.wrap(Math.toDegrees(headingToCursor.angle(up)) as float, 0f, 360f)
			}
		}

		/**
		 * Update player movement and position based on inputs.
		 */
		private void updateMovement(float delta) {

			// Set the direction of the movement force based on inputs
			var impulseDirection = 0f
			if (input.keyPressed(GLFW_KEY_W)) {
				impulseDirection =
					input.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(node.heading - 45f)) :
						input.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(node.heading + 45f)) :
							node.heading
				node.accelerating = true
			}
			else if (input.keyPressed(GLFW_KEY_S)) {
				impulseDirection =
					input.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(node.heading - 135f)) :
						input.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(node.heading + 135f)) :
							node.heading + 180f
				node.accelerating = true
			}
			else if (input.keyPressed(GLFW_KEY_A)) {
				impulseDirection = Math.wrapToCircle((float)(node.heading - 90f))
				node.accelerating = true
			}
			else if (input.keyPressed(GLFW_KEY_D)) {
				impulseDirection = Math.wrapToCircle((float)(node.heading + 90f))
				node.accelerating = true
			}
			else {
				node.accelerating = false
			}

			// Adjust the strength of the force based on acceleration time
			if (node.accelerating) {
				var impulseDirectionInRadians = Math.toRadians(impulseDirection)
				vector.set(Math.sin(impulseDirectionInRadians), Math.cos(impulseDirectionInRadians)).normalize().mul(MAX_SPEED).mul(delta)
			}
			else {
				vector.set(0f, 0f)
			}

			// Calculate the velocity from the above
			node.velocity.lerp(vector, 0.5f * delta as float)

			// Adjust for collisions with screen edges
			// TODO: Have this baked into a movement node with colliders? 🤔
			if (hitLeftScreenEdge && node.velocity.x < 0f) {
				node.velocity.x = 0f
			}
			if (hitRightScreenEdge && node.velocity.x > 0f) {
				node.velocity.x = 0f
			}
			if (hitTopScreenEdge && node.velocity.y > 0f) {
				node.velocity.y = 0f
			}
			if (hitBottomScreenEdge && node.velocity.y < 0f) {
				node.velocity.y = 0f
			}

			// Adjust position based on velocity
			// TODO: Have this handled by the movement node
			if (node.velocity) {
				node.translate(node.velocity.x, node.velocity.y, 0f)
			}
		}
	}
}
