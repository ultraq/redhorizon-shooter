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

import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.PaletteAlphaMask
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.shooter.ScopedValues.*

import org.joml.primitives.Rectanglef

/**
 * Set up and update via scripts the scene of the game.
 *
 * @author Emanuel Rabina
 */
class ShooterScene extends Scene implements AutoCloseable {

	private static final Colour GRID_LINES_ORIGIN = new Colour('GridLines-Origin', 0.2, 0.2, 0.2)
	private static final Colour GRID_LINES_DIVIDERS = new Colour('GridLines-Dividers', 0.6, 0.6, 0.6)

	final int width
	final int height
	final Camera camera
	final Player player
	boolean showCollisionLines = false

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	ShooterScene(int width, int height) {

		this.width = width
		this.height = height

		var window = WINDOW.get()
		camera = new Camera(width, height, window)

		addChild(camera)
		addChild(new GridLines(new Rectanglef(0, 0, width, height).center(), 24f,
			GRID_LINES_ORIGIN, GRID_LINES_DIVIDERS))

		var resourceManager = RESOURCE_MANAGER.get()
		addChild(resourceManager.loadPalette('temperat-td.pal')
			.withName('Palette'))
		addChild(new PaletteAlphaMask()
			.withName('Alpha mask'))

		player = new Player()
		addChild(player)
	}
}
