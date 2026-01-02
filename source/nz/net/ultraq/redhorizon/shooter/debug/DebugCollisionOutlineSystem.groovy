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

package nz.net.ultraq.redhorizon.shooter.debug

import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.System
import nz.net.ultraq.redhorizon.engine.graphics.MeshComponent
import nz.net.ultraq.redhorizon.engine.physics.CircleCollisionComponent
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Mesh.Type
import nz.net.ultraq.redhorizon.graphics.Vertex
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.shooter.ShooterScene

import org.joml.Vector3f

/**
 * Manage the drawing of collision outlines for debuggin.
 *
 * @author Emanuel Rabina
 */
class DebugCollisionOutlineSystem extends System {

	private static final String COLLISION_OUTLINE_NAME = 'Collision outline'

	@Override
	void update(Scene scene, float delta) {

		scene.traverse(Entity) { Entity entity ->
			var collision = entity.findComponentByType(CircleCollisionComponent) as CircleCollisionComponent
			var collisionOutline = entity.findComponent { it.name == COLLISION_OUTLINE_NAME } as MeshComponent
			if (((ShooterScene)scene).showCollisionLines) {
				if (collision) {
					if (!collisionOutline) {
						var radius = collision.radius
						collisionOutline = entity.addAndReturnComponent(
							new MeshComponent(Type.LINE_LOOP, new Vertex[]{
								new Vertex(new Vector3f(-radius as float, -radius as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(radius as float, -radius as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(radius as float, radius as float, 0), Colour.YELLOW),
								new Vertex(new Vector3f(-radius as float, radius as float, 0), Colour.YELLOW)
							})
								.withName(COLLISION_OUTLINE_NAME)
						)
					}
					if (collision.enabled) {
						collisionOutline.enable()
					}
					else {
						collisionOutline.disable()
					}
				}
			}
			else {
				if (collisionOutline) {
					collisionOutline.disable()
				}
			}
		}
	}
}
