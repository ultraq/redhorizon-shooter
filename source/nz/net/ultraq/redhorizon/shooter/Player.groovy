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
import nz.net.ultraq.redhorizon.graphics.PaletteAlphaMask
import nz.net.ultraq.redhorizon.graphics.PaletteSwapMap
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.physics.BoxCollider
import nz.net.ultraq.redhorizon.physics.CollisionEndEvent
import nz.net.ultraq.redhorizon.physics.CollisionStartEvent
import nz.net.ultraq.redhorizon.physics.MovementNode
import nz.net.ultraq.redhorizon.runtime.objects.ScreenEdges
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.*

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
class Player extends Node<Player> {

	// TODO: These should come from the unit data for the orca sprite
	final int headings = 32
	final float headingStep = 360f / headings as float

	float maxSpeed = 200f
	float bobbingAmplitude = 8f

	// Player properties adjustable by scripts
	// TODO: These can be moved into their own components if other objects need them
	boolean flying = true
	float heading = 25f
	boolean accelerating = false
	float rateOfFire = 0.1f

	/**
	 * Constructor, create a new player object.
	 */
	Player() {

		var resourceManager = RESOURCE_MANAGER.get()

		addChild(resourceManager.loadPalette('temperat-td.pal')
			.withName('Palette'))
		addChild(new PaletteAlphaMask()
			.withName('Alpha mask'))
		addChild(new PaletteSwapMap(Faction.GOLD.colours)
			.withName('Faction - Gold'))

		var orcaSpriteSheet = resourceManager.loadSpriteSheet('orca.shp')
		addChild(new Sprite(orcaSpriteSheet, PalettedSpriteShader)
			.translate(0f, 24f, 0f)
			.withName('Orca')
			.addChild(new BoxCollider(orcaSpriteSheet.width, orcaSpriteSheet.height)
				.withName('Sprite collider')))
		addChild(new Sprite(orcaSpriteSheet, ShadowShader)
			.withName('Shadow'))
		addChild(new MovementNode(maxSpeed))
		addChild(new BoxCollider(orcaSpriteSheet.width, orcaSpriteSheet.height)
			.withName('Base collider'))

		addChild(new ScriptNode(PlayerScript))
	}

	/**
	 * A script for controlling the player sprite's bobbing motion.
	 */
	static class PlayerScript extends Script<Player> {

		private static final Logger logger = LoggerFactory.getLogger(PlayerScript)

		// Bobbing
		private float bobbingTimer = 0f

		// Heading/rotation
		private Vector3f unprojectResult = new Vector3f()
		private Vector2f worldCursorPosition = new Vector2f()
		private Vector2f positionXY = new Vector2f()
		private Vector2f headingToCursor = new Vector2f()

		// Movement
		private final Vector2f vector = new Vector2f()
		private boolean hitLeftScreenEdge = false
		private boolean hitRightScreenEdge = false
		private boolean hitTopScreenEdge = false
		private boolean hitBottomScreenEdge = false

		// Shooting
		private float firingCooldown = 0f
		private final Vector2f bulletInitialVelocity = new Vector2f()

		@Override
		void init() {

			(node.find('Base collider') as BoxCollider)
				.on(CollisionStartEvent) { event ->
					var otherCollider = event.otherCollider()
					if (otherCollider.parent instanceof ScreenEdges) {
						if (otherCollider.name == ScreenEdges.TOP_COLLIDER_NAME) {
							hitTopScreenEdge = true
							logger.debug('Hitting top screen edge')
						}
						else if (otherCollider.name == ScreenEdges.BOTTOM_COLLIDER_NAME) {
							hitBottomScreenEdge = true
							logger.debug('Hitting bottom screen edge')
						}
						if (otherCollider.name == ScreenEdges.LEFT_COLLIDER_NAME) {
							hitLeftScreenEdge = true
							logger.debug('Hitting left screen edge')
						}
						else if (otherCollider.name == ScreenEdges.RIGHT_COLLIDER_NAME) {
							hitRightScreenEdge = true
							logger.debug('Hitting right screen edge')
						}
					}
				}
				.on(CollisionEndEvent) { event ->
					var otherCollider = event.otherCollider()
					var otherObject = otherCollider.parent
					if (otherObject instanceof ScreenEdges) {
						if (otherCollider.name == ScreenEdges.TOP_COLLIDER_NAME) {
							hitTopScreenEdge = false
							logger.debug('No longer hitting top screen edge')
						}
						else if (otherCollider.name == ScreenEdges.BOTTOM_COLLIDER_NAME) {
							hitBottomScreenEdge = false
							logger.debug('No longer hitting bottom screen edge')
						}
						if (otherCollider.name == ScreenEdges.LEFT_COLLIDER_NAME) {
							hitLeftScreenEdge = false
							logger.debug('No longer hitting left screen edge')
						}
						else if (otherCollider.name == ScreenEdges.RIGHT_COLLIDER_NAME) {
							hitRightScreenEdge = false
							logger.debug('No longer hitting right screen edge')
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
			updateShooting(delta)
		}

		/**
		 * Adjust player position to simulate an aircraft bobbing up and down.
		 */
		private void updateBobbing(float delta) {

			if (node.flying) {
				bobbingTimer += delta
				node.find('Orca').setPosition(0f, 24f + (Math.sin(bobbingTimer) * node.bobbingAmplitude) as float, 0f)
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
			node.findAll(Sprite)*.withFramePosition(frame)
		}

		/**
		 * Update player heading and sprite to always face the cursor.
		 */
		private void updateHeading() {

			var window = WINDOW.get()
			var cursorPosition = input.cursorPosition()
			if (cursorPosition) {
				var camera = node.scene.find(Camera)
				positionXY.set(node.position)
				worldCursorPosition.set(camera.unproject(window.viewport, cursorPosition.x(), cursorPosition.y(), unprojectResult))
				worldCursorPosition.sub(positionXY, headingToCursor)
				node.heading = Math.wrap(Math.toDegrees(headingToCursor.angle(Vector2f.UP)) as float, 0f, 360f)
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
				vector.set(Math.sin(impulseDirectionInRadians), Math.cos(impulseDirectionInRadians)).normalize()
					.mul(node.maxSpeed).mul(delta)
			}
			else {
				vector.set(0f, 0f)
			}

			// Calculate the velocity from the above
			// TODO: Add inertia calculation to the movement system
			var movement = node.find(MovementNode)
			movement.vector.lerp(vector, 0.5f * delta as float)

			// Adjust for collisions with screen edges
			// TODO: Have this baked into a movement node with colliders? 🤔
			if (hitLeftScreenEdge) {
				movement.vector.x = Math.max(movement.vector.x, 0f)
			}
			if (hitRightScreenEdge) {
				movement.vector.x = Math.min(movement.vector.x, 0f)
			}
			if (hitTopScreenEdge) {
				movement.vector.y = Math.min(movement.vector.y, 0f)
			}
			if (hitBottomScreenEdge) {
				movement.vector.y = Math.max(movement.vector.y, 0f)
			}
		}

		/**
		 * Fire bullets.
		 */
		private void updateShooting(float delta) {

			firingCooldown -= delta

			if ((input.keyPressed(GLFW_KEY_SPACE) || input.mouseButtonPressed(GLFW_MOUSE_BUTTON_1)) && firingCooldown <= 0f) {
				var scene = node.scene
				var movement = node.find(MovementNode)
				scene.queueUpdate { ->
					scene.find('Bullets').addChild(
						new Bullet(
							node.find('Orca').globalTransform,
							node.heading,
							movement.vector.mul(movement.speed, bulletInitialVelocity))
					)
				}
				firingCooldown = node.rateOfFire
			}
		}
	}
}
