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

import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader.PalettedSpriteShaderContext
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.shooter.utilities.ResourceManager

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements GameObject, AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Player)

	private final Palette tdPalette
	private final SpriteSheet orcaSpriteSheet
	private final Sprite orca
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
		addChild(orca)
	}

	@Override
	void close() {

		orca?.close()
	}

	/**
	 * Draw the player.
	 */
	void render(PalettedSpriteShaderContext shaderContext) {

		shaderContext.setPalette(tdPalette)
		orca.draw(shaderContext, orcaSpriteSheet.getFramePosition(frame))
	}

	@Override
	void update(float delta) {

		// Helicopter bobbing
		bobbingTimer += delta
		var bob = 0.0625f * Math.sin(bobbingTimer) as float
		translate(0f, bob, 0f)
		// TODO: Node global positions so that we only need to adjust the parent
		orca.translate(0f, bob, 0f)
	}
}
