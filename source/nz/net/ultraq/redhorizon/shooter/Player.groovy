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

import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.shooter.engine.GameObject
import nz.net.ultraq.redhorizon.shooter.engine.GraphicsContext
import nz.net.ultraq.redhorizon.shooter.engine.GraphicsObject
import nz.net.ultraq.redhorizon.shooter.utilities.ResourceManager

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements GameObject, GraphicsObject, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Player)

	final String name = 'Player'
	private final Palette tdPalette
	private final SpriteSheet orcaSpriteSheet
	private final Sprite orca
	private final Sprite shadow
	private float bobbingTimer
	private int frame = 0

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
	void update(float delta) {

		// Helicopter bobbing
		bobbingTimer += delta
		orca.translate(0f, 0.0625f * Math.sin(bobbingTimer) as float, 0f)
	}
}
