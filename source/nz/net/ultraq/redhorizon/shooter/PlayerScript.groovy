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

import nz.net.ultraq.redhorizon.engine.GameObjectScript
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.input.InputEventHandler

import org.joml.Vector2f
import org.joml.Vector3f
import static org.lwjgl.glfw.GLFW.*

/**
 * A script for controlling the player sprite's bobbing motion.
 *
 * @author Emanuel Rabina
 */
class PlayerScript implements GameObjectScript<Player> {

	// TODO: Make these public items into variables that can be controlled by ImGui?
	static final float MAX_SPEED = 400f
	static final float TIME_TO_MAX_SPEED_S = 1
	private static final Vector2f up = new Vector2f(0, 1)

	Camera camera
	InputEventHandler inputEventHandler

	// Bobbing
	private float bobbingTimer = 0f

	// Heading/rotation
	private Vector2f lastCursorPosition = new Vector2f()
	private Vector3f unprojectResult = new Vector3f()
	private Vector2f worldCursorPosition = new Vector2f()
	private Vector2f positionXY = new Vector2f()
	private Vector2f headingToCursor = new Vector2f()

	// Movement
	private Vector2f impulse = new Vector2f()
	private float accAccelerationTime = 0f

	@Override
	void update(Player player, float delta) {

		updateBobbing(player, delta)
		updateHeading(player, delta)
		updateMovement(player, delta)
	}

	/**
	 * Adjust player position to simulate an aircraft bobbing up and down.
	 */
	private void updateBobbing(Player player, float delta) {

		if (player.flying) {
			bobbingTimer += delta
			player.orca.translate(0f, 0.0625f * Math.sin(bobbingTimer) as float, 0f)
		}
	}

	/**
	 * Update player heading and sprite to always face the cursor.
	 */
	private void updateHeading(Player player, float delta) {

		var cursorPosition = inputEventHandler.cursorPosition()
		if (cursorPosition && cursorPosition != lastCursorPosition) {
			positionXY.set(player.position)
			worldCursorPosition.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
			worldCursorPosition.sub(positionXY, headingToCursor)
			player.heading = Math.wrap(Math.toDegrees(headingToCursor.angle(up)) as float, 0f, 360f)

			lastCursorPosition.set(cursorPosition)
		}
	}

	/**
	 * Update player movement and position based on inputs.
	 */
	private void updateMovement(Player player, float delta) {

		// Set the direction of the movement force based on inputs
		var impulseDirection = 0f
		if (inputEventHandler.keyPressed(GLFW_KEY_W)) {
			impulseDirection =
				inputEventHandler.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(player.heading - 45f)) :
					inputEventHandler.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(player.heading + 45f)) :
						player.heading
			player.accelerating = true
		}
		else if (inputEventHandler.keyPressed(GLFW_KEY_S)) {
			impulseDirection =
				inputEventHandler.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(player.heading - 135f)) :
					inputEventHandler.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(player.heading + 135f)) :
						player.heading + 180f
			player.accelerating = true
		}
		else if (inputEventHandler.keyPressed(GLFW_KEY_A)) {
			impulseDirection = Math.wrapToCircle((float)(player.heading - 90f))
			player.accelerating = true
		}
		else if (inputEventHandler.keyPressed(GLFW_KEY_D)) {
			impulseDirection = Math.wrapToCircle((float)(player.heading + 90f))
			player.accelerating = true
		}
		else {
			player.accelerating = false
		}

		// Adjust the strength of the force based on acceleration time
		if (player.accelerating) {
			var impulseDirectionInRadians = Math.toRadians(impulseDirection)
			impulse.set(Math.sin(impulseDirectionInRadians), Math.cos(impulseDirectionInRadians)).normalize().mul(MAX_SPEED).mul(delta)
			accAccelerationTime = Math.min((float)(accAccelerationTime + delta), TIME_TO_MAX_SPEED_S)
		}
		else {
			impulse.set(0f, 0f)
			accAccelerationTime = Math.max((float)(accAccelerationTime - delta), 0f)
		}

		// Calculate the velocity from the above
		player.velocity.lerp(impulse, 0.5f * delta as float)

		// Adjust position based on velocity
		if (player.velocity) {
			// TODO: Clamp player position to world bounds using Vector2f.min/max
			player.translate(player.velocity.x, player.velocity.y, 0)
		}
	}
}
