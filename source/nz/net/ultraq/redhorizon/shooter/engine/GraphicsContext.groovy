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

package nz.net.ultraq.redhorizon.shooter.engine

import nz.net.ultraq.redhorizon.classic.graphics.AlphaMask
import nz.net.ultraq.redhorizon.classic.graphics.FactionAdjustmentMap
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.shooter.utilities.ShaderManager

import groovy.transform.ImmutableOptions

/**
 * A collection of objects that are useful for rendering.  Created so we don't
 * have to pass so much stuff around in constructors.
 *
 * @author Emanuel Rabina
 */
@ImmutableOptions(knownImmutables = ['shaderManager', 'camera', 'adjustmentMap', 'alphaMask'])
record GraphicsContext(ShaderManager shaderManager, Camera camera, FactionAdjustmentMap adjustmentMap, AlphaMask alphaMask) {

	BasicShader getBasicShader() {
		return shaderManager.basicShader
	}

	PalettedSpriteShader getPalettedSpriteShader() {
		return shaderManager.palettedSpriteShader
	}

	ShadowShader getShadowShader() {
		return shaderManager.shadowShader
	}
}
