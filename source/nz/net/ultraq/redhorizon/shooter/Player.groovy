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

import nz.net.ultraq.redhorizon.engine.GameObject
import nz.net.ultraq.redhorizon.engine.GraphicsObject
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.shooter.engine.ShooterGraphicsContext

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends GameObject<Player> implements GraphicsObject<ShooterGraphicsContext>, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Player)

	// Unit variables
	// TODO: Move to ImGui controls?
	static final float MAX_SPEED = 200f
	static final float TIME_TO_MAX_SPEED_S = 1
	private static final float ROTATION_SPEED = 180f

	// TODO: These should come from the unit data for the orca sprite
	private static final int headings = 32
	private static final float headingStep = 360f / headings as float

	final String name = 'Player'

	// Player properties adjustable by scripts
	boolean flying = true
	float heading = 25f
	boolean accelerating = false
	final Vector2f velocity = new Vector2f()

	// Rendering
	private final Palette tdPalette
	private final SpriteSheet orcaSpriteSheet
	final Sprite orca
	final Sprite shadow

	/**
	 * Constructor, create a new player object.
	 */
	Player(ResourceManager resourceManager) {

		tdPalette = resourceManager.loadPalette('temperat-td.pal')
		orcaSpriteSheet = resourceManager.loadSpriteSheet('orca.shp')
		orca = new Sprite(orcaSpriteSheet)
			.translate(-18f, -12f, 0f)
			.withName('Orca')
		shadow = new Sprite(orcaSpriteSheet)
			.translate(-18f, -36f, 0f)
			.withName('Shadow')

		addChild(orca)
		addChild(shadow)
	}

	@Override
	void close() {

		orca?.close()
		shadow?.close()
	}

	@Override
	void render(ShooterGraphicsContext context) {

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
}
