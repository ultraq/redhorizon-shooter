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

package nz.net.ultraq.redhorizon.shooter.utilities

import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader

/**
 * Manage the shaders used by the game.
 *
 * @author Emanuel Rabina
 */
class ShaderManager implements AutoCloseable {

	final BasicShader basicShader
	final PalettedSpriteShader palettedSpriteShader
	final ShadowShader shadowShader

	/**
	 * Constructor, load the game's shaders.
	 */
	ShaderManager() {

		basicShader = new BasicShader()
		palettedSpriteShader = new PalettedSpriteShader()
		shadowShader = new ShadowShader()
	}

	@Override
	void close() {

		basicShader?.close()
		palettedSpriteShader?.close()
		shadowShader?.close()
	}
}
