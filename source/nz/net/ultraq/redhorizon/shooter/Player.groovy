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
import nz.net.ultraq.redhorizon.engine.scripts.ScriptNode
import nz.net.ultraq.redhorizon.graphics.PaletteSwapMap
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.scenegraph.Node
import static nz.net.ultraq.redhorizon.shooter.ScopedValues.RESOURCE_MANAGER

import org.joml.Vector2f

/**
 * The player object in the game.
 *
 * @author Emanuel Rabina
 */
class Player extends Node<Player> implements AutoCloseable {

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

	/**
	 * Constructor, create a new player object.
	 */
	Player() {

		addChild(new PaletteSwapMap(Faction.GOLD.colours)
			.withName('Faction - Gold'))

		var resourceManager = RESOURCE_MANAGER.get()
		var orcaSpriteSheet = resourceManager.loadSpriteSheet('orca.shp')
		addChild(new Sprite(orcaSpriteSheet, PalettedSpriteShader)
			.translate(0f, 24f, 0f)
			.withName('Orca'))
		addChild(new Sprite(orcaSpriteSheet, ShadowShader)
			.withName('Shadow'))

		addChild(new ScriptNode(PlayerScript))
	}
}
