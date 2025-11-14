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

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.shooter.animation.EasingFunctions
import nz.net.ultraq.redhorizon.shooter.engine.GameContext
import nz.net.ultraq.redhorizon.shooter.engine.GameObject
import nz.net.ultraq.redhorizon.shooter.engine.GraphicsContext
import nz.net.ultraq.redhorizon.shooter.engine.GraphicsObject
import nz.net.ultraq.redhorizon.shooter.utilities.ResourceManager

import org.joml.Vector2f
import org.joml.Vector3f
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import static org.lwjgl.glfw.GLFW.*

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements GameObject, GraphicsObject, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Player)

	// Unit variables
	// TODO: Move to ImGui controls?
	private static final float MAX_SPEED = 200f
	private static final float TIME_TO_MAX_SPEED_S = 1
	private static final float ROTATION_SPEED = 180f
	private static final Vector2f up = new Vector2f(0, 1)

	// TODO: These should come from the unit data for the orca sprite
	private static final int headings = 32
	private static final float headingStep = 360f / headings as float

	final String name = 'Player'

	// Rendering
	private final Palette tdPalette
	private final SpriteSheet orcaSpriteSheet
	private final Sprite orca
	private final Sprite shadow

	// Bobbing
	private float bobbingTimer = 0f

	// Rotation
	private Vector2f lastCursorPosition = new Vector2f()
	private Vector3f unprojectResult = new Vector3f()
	private Vector2f worldCursorPosition = new Vector2f()
	private Vector2f positionXY = new Vector2f()
	private Vector2f headingToCursor = new Vector2f()
	private float heading = 0f

	// Movement
	private boolean accelerating = false
	private Vector2f impulse = new Vector2f()
	private Vector2f velocity = new Vector2f()
	private float accAccelerationTime = 0f

	/**
	 * Constructor, create a new player object.
	 */
	Player(ResourceManager resourceManager) {

		tdPalette = resourceManager.loadPalette('temperat-td.pal')
		orcaSpriteSheet = resourceManager.loadSpriteSheet('orca.shp')
		orca = new Sprite(orcaSpriteSheet)
			.translate(-18f, -12f, 0f)
		shadow = new Sprite(orcaSpriteSheet)
			.translate(-18f, -36f, 0f)

		orca.name = 'Orca'
		shadow.name = 'Shadow'
		addChild(orca)
		addChild(shadow)
	}

	@Override
	void close() {

		orca?.close()
		shadow?.close()
	}

	@Override
	void render(GraphicsContext context) {

		// NOTE: C&C unit headings were ordered in a counter-clockwise order, the
		//       reverse from how degrees-based headings are done.
		var closestHeading = Math.round(heading / headingStep)
		var frame = closestHeading ? headings - closestHeading as int : 0
		if (accelerating) {
			frame += headings
		}

		var framePosition = orcaSpriteSheet.getFramePosition(frame)
		context.shadowShader.useShader { shaderContext ->
			context.camera().update(shaderContext)
			shadow.draw(shaderContext, framePosition)
		}
		context.palettedSpriteShader.useShader { shaderContext ->
			context.camera().update(shaderContext)
			shaderContext.setAdjustmentMap(context.adjustmentMap())
			shaderContext.setAlphaMask(context.alphaMask())
			shaderContext.setPalette(tdPalette)
			orca.draw(shaderContext, framePosition)
		}
	}

	@Override
	void update(float delta, GameContext context) {

		var inputEventHandler = context.inputEventHandler()
		updateBobbing(delta)
		updateHeading(delta, inputEventHandler.cursorPosition(), context.camera())
		updateMovement(delta, inputEventHandler)
	}

	/**
	 * Adjust player position to simulate an aircraft bobbing up and down.
	 */
	private void updateBobbing(float delta) {

		bobbingTimer += delta
		orca.translate(0f, 0.0625f * Math.sin(bobbingTimer) as float, 0f)
	}

	/**
	 * Update player heading and sprite to always face the cursor.
	 */
	private void updateHeading(float delta, Vector2f cursorPosition, Camera camera) {

		if (cursorPosition && cursorPosition != lastCursorPosition) {
			positionXY.set(position)
			worldCursorPosition.set(camera.unproject(cursorPosition.x(), cursorPosition.y(), unprojectResult))
			worldCursorPosition.sub(positionXY, headingToCursor)
			heading = Math.wrap(Math.toDegrees(headingToCursor.angle(up)) as float, 0f, 360f)

			lastCursorPosition.set(cursorPosition)
		}
	}

	/**
	 * Update player movement and position based on inputs.
	 */
	private void updateMovement(float delta, InputEventHandler inputEventHandler) {

		// Set the direction of the movement force based on inputs
		var impulseDirection = 0f
		if (inputEventHandler.keyPressed(GLFW_KEY_W)) {
			impulseDirection =
				inputEventHandler.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(heading - 45f)) :
					inputEventHandler.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(heading + 45f)) :
						heading
			accelerating = true
		}
		else if (inputEventHandler.keyPressed(GLFW_KEY_S)) {
			impulseDirection =
				inputEventHandler.keyPressed(GLFW_KEY_A) ? Math.wrapToCircle((float)(heading - 135f)) :
					inputEventHandler.keyPressed(GLFW_KEY_D) ? Math.wrapToCircle((float)(heading + 135f)) :
						heading + 180f
			accelerating = true
		}
		else if (inputEventHandler.keyPressed(GLFW_KEY_A)) {
			impulseDirection = Math.wrapToCircle((float)(heading - 90f))
			accelerating = true
		}
		else if (inputEventHandler.keyPressed(GLFW_KEY_D)) {
			impulseDirection = Math.wrapToCircle((float)(heading + 90f))
			accelerating = true
		}
		else {
			accelerating = false
		}

		// Adjust the strength of the force based on acceleration time
		if (accelerating) {
			var impulseDirectionInRadians = Math.toRadians(impulseDirection)
			impulse.set(Math.sin(impulseDirectionInRadians), Math.cos(impulseDirectionInRadians)).mul(MAX_SPEED).normalize()
			accAccelerationTime = Math.min((float)(accAccelerationTime + delta), TIME_TO_MAX_SPEED_S)
		}
		else {
			accAccelerationTime = Math.max((float)(accAccelerationTime - delta), 0f)
		}

		// Calculate the velocity from the above
		velocity.set(0f, 0f).lerp(impulse, EasingFunctions.linear(accAccelerationTime))

		// Adjust position based on velocity
		if (velocity) {
			// TODO: Have parent node transform affect children
//			var worldBounds = context.worldBounds()
//			setPosition(
//				Math.clamp(movement.x, worldBounds.minX, worldBounds.maxX),
//				Math.clamp(movement.y, worldBounds.minY, worldBounds.maxY),
//				0
//			)
			this.translate(velocity.x, velocity.y, 0)
			orca.translate(velocity.x, velocity.y, 0)
			shadow.translate(velocity.x, velocity.y, 0)
		}
	}
}
