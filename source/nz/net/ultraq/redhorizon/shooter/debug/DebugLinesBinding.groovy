/*
 * Copyright 2026, Emanuel Rabina (http://www.ultraq.net.nz/)
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

package nz.net.ultraq.redhorizon.shooter.debug

import nz.net.ultraq.redhorizon.engine.graphics.GridLinesEntity
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.input.KeyBinding
import nz.net.ultraq.redhorizon.shooter.ShooterScene

import org.joml.primitives.Rectanglef
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L

/**
 * Input binding for toggling grid lines with the {@code L} key.
 *
 * @author Emanuel Rabina
 */
class DebugLinesBinding extends KeyBinding {

	private static final Colour LIGHT_GREY = new Colour('Light grey', 0.85f, 0.85f, 0.85f, 1f)

	private static boolean enabled = false

	DebugLinesBinding(ShooterScene scene) {

		super(GLFW_KEY_L, true, { ->
			enabled = !enabled

			var gridLines = scene.findDescendent { it instanceof GridLinesEntity } as GridLinesEntity
			if (enabled) {
				if (!gridLines) {
					scene.insertBefore(
						new GridLinesEntity(new Rectanglef(0f, 0f, scene.width, scene.height).center(), 100f,
							LIGHT_GREY, Colour.GREY)
							.withName('Grid lines'),
						scene.player)
				}
				else {
					gridLines.enable()
				}
			}
			else {
				if (gridLines) {
					gridLines.disable()
				}
			}

			scene.showCollisionLines = !scene.showCollisionLines
		})
	}
}
