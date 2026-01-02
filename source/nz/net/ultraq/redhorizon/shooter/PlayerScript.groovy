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

import nz.net.ultraq.redhorizon.engine.graphics.SpriteComponent
import nz.net.ultraq.redhorizon.engine.scripts.EntityScript

import org.joml.Vector2f
import org.joml.Vector3f
import static org.lwjgl.glfw.GLFW.*

/**
 * A script for controlling the player sprite's bobbing motion.
 *
 * @author Emanuel Rabina
 */
class PlayerScript extends EntityScript<Player> {

	// TODO: Make these public items into variables that can be controlled by ImGui?
	static final float MAX_SPEED = 400f
	static final float TIME_TO_MAX_SPEED_S = 1
	private static final Vector2f up = new Vector2f(0, 1)

	private Vector2f worldBoundsMin
	private Vector2f worldBoundsMax

	// Bobbing
	private float bobbingTimer = 0f

	// Heading/rotation
	private Vector3f unprojectResult = new Vector3f()
	private Vector2f worldCursorPosition = new Vector2f()
	private Vector2f positionXY = new Vector2f()
	private Vector2f headingToCursor = new Vector2f()

	// Movement
	private Vector2f impulse = new Vector2f()
	private Vector2f updatedPosition = new Vector2f()

	@Override
	void init() {

		var scene = entity.scene as ShooterScene
		worldBoundsMin = new Vector2f(-scene.width / 2f as float, -scene.height / 2f as float)
		worldBoundsMax = new Vector2f(scene.width / 2f as float, scene.height / 2f as float)
	}

	@Override
	void update(float delta) {

		updateBobbing(delta)
		updateHeading()
		updateMovement(delta)
		updateFramePosition(delta)
	}

	/**
	 * Adjust player position to simulate an aircraft bobbing up and down.
	 */
	private void updateBobbing(float delta) {

		if (entity.flying) {
			bobbingTimer += delta
			var orcaSprite = entity.findComponent { it.name == 'Orca' } as SpriteComponent
			var position = orcaSprite.position
			orcaSprite.setPosition(position.x(), 24f + (Math.sin(bobbingTimer) * 8) as float, position.z())
		}
	}

	/**
	 * Adjust the selected sprite frame in each of the player's sprite components.
	 */
	private void updateFramePosition(float delta) {

		// NOTE: C&C unit headings were ordered in a counter-clockwise order, the
		//       reverse from how degrees-based headings are done.
		var closestHeading = Math.round(entity.heading / entity.headingStep)
		var frame = closestHeading ? entity.headings - closestHeading as int : 0
		if (entity.accelerating) {
			frame += entity.headings
		}

		entity.findComponentsByType(SpriteComponent).each { component ->
			component.framePosition.set(component.spriteSheet.getFramePosition(frame))
		}
	}

	/**
	 * Update player heading and sprite to always face the cursor.
	 */
	private void updateHeading() {

		var cursorPosition = input.cursorPosition()
		if (cursorPosition) {
			var camera = ((ShooterScene)entity.scene).camera
			positionXY.set(entity.position)
			worldCursorPosition.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
			worldCursorPosition.sub(positionXY, headingToCursor)
			entity.heading = Math.wrap(Math.toDegrees(headingToCursor.angle(up)) as float, 0f, 360f)
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
				input.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(entity.heading - 45f)) :
					input.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(entity.heading + 45f)) :
						entity.heading
			entity.accelerating = true
		}
		else if (input.keyPressed(GLFW_KEY_S)) {
			impulseDirection =
				input.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(entity.heading - 135f)) :
					input.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(entity.heading + 135f)) :
						entity.heading + 180f
			entity.accelerating = true
		}
		else if (input.keyPressed(GLFW_KEY_A)) {
			impulseDirection = Math.wrapToCircle((float)(entity.heading - 90f))
			entity.accelerating = true
		}
		else if (input.keyPressed(GLFW_KEY_D)) {
			impulseDirection = Math.wrapToCircle((float)(entity.heading + 90f))
			entity.accelerating = true
		}
		else {
			entity.accelerating = false
		}

		// Adjust the strength of the force based on acceleration time
		if (entity.accelerating) {
			var impulseDirectionInRadians = Math.toRadians(impulseDirection)
			impulse.set(Math.sin(impulseDirectionInRadians), Math.cos(impulseDirectionInRadians)).normalize().mul(MAX_SPEED).mul(delta)
		}
		else {
			impulse.set(0f, 0f)
		}

		// Calculate the velocity from the above
		entity.velocity.lerp(impulse, 0.5f * delta as float)

		// Adjust position based on velocity
		if (entity.velocity) {
			updatedPosition.set(entity.position).add(entity.velocity).min(worldBoundsMax).max(worldBoundsMin)
			entity.setPosition(updatedPosition.x(), updatedPosition.y(), 0)
		}
	}
}
