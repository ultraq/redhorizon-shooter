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
import nz.net.ultraq.redhorizon.classic.graphics.AlphaMask
import nz.net.ultraq.redhorizon.classic.graphics.FactionAdjustmentMap
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.engine.ScriptEngine
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.shooter.engine.Entity
import nz.net.ultraq.redhorizon.shooter.engine.ScriptComponent
import nz.net.ultraq.redhorizon.shooter.engine.SpriteComponent

import org.joml.Vector2f
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Entity<Player> implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(Player)

	// Unit variables
	// TODO: Move to ImGui controls?
	static final float MAX_SPEED = 200f
	static final float TIME_TO_MAX_SPEED_S = 1
	private static final float ROTATION_SPEED = 180f

	// TODO: These should come from the unit data for the orca sprite
	final int headings = 32
	final float headingStep = 360f / headings as float

	final String name = 'Player'

	// Player properties adjustable by scripts
	// TODO: These can be moved into their own components if other objects need them
	boolean flying = true
	float heading = 25f
	boolean accelerating = false
	final Vector2f velocity = new Vector2f()

	// TODO: If needing to share, these could be components too
	private final FactionAdjustmentMap adjustmentMap
	private final AlphaMask alphaMask

	/**
	 * Constructor, create a new player object.
	 */
	Player(int sceneWidth, int sceneHeight, ResourceManager resourceManager, Palette palette, ScriptEngine scriptEngine,
		InputEventHandler inputEventHandler) {

		adjustmentMap = new FactionAdjustmentMap(Faction.GOLD)
		alphaMask = new AlphaMask()

		var orcaSpriteSheet = resourceManager.loadSpriteSheet('orca.shp')
		addComponent(new SpriteComponent('Orca', orcaSpriteSheet, PalettedSpriteShader, palette, adjustmentMap, alphaMask)
			.translate(-18f, 12f, 0f))
		addComponent(new SpriteComponent('Shadow', orcaSpriteSheet, ShadowShader)
			.translate(-18f, -12f, 0f))

		addComponent(new ScriptComponent(scriptEngine, 'PlayerScript.groovy', [
			inputEventHandler: inputEventHandler,
			worldBoundsMin: new Vector2f(-sceneWidth / 2f as float, -sceneHeight / 2f as float),
			worldBoundsMax: new Vector2f(sceneWidth / 2f as float, sceneHeight / 2f as float)
		]))
	}

	@Override
	void close() {

		super.close()
		adjustmentMap.close()
		alphaMask.close()
	}
}
