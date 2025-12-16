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
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader.PalettedSpriteShaderContext
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet

import org.joml.Matrix4f
import org.joml.Vector2f

/**
 * A component for adding a {@link Sprite} to an entity.
 *
 * @author Emanuel Rabina
 */
class SpriteComponent extends GraphicsComponent implements AutoCloseable {

	final String name
	final Sprite sprite
	final Vector2f framePosition = new Vector2f()
	final Matrix4f transform = new Matrix4f()
	final SpriteSheet spriteSheet
	final Class<Shader> shaderClass
	private final Palette palette
	private final FactionAdjustmentMap adjustmentMap
	private final AlphaMask alphaMask
	private final Matrix4f globalTransform = new Matrix4f()

	/**
	 * Constructor, use the given sprite sheet for the sprite.
	 */
	SpriteComponent(String name, SpriteSheet spriteSheet, Class<Shader> shaderClass, Palette palette = null,
		FactionAdjustmentMap adjustmentMap = null, AlphaMask alphaMask = null) {

		this.name = name
		this.spriteSheet = spriteSheet
		this.shaderClass = shaderClass
		this.palette = palette
		this.adjustmentMap = adjustmentMap
		this.alphaMask = alphaMask

		sprite = new Sprite(spriteSheet)
	}

	@Override
	void close() {

		sprite.close()
	}

	/**
	 * Render the sprite.
	 */
	@Override
	void render(SceneShaderContext shaderContext) {

		if (shaderContext instanceof PalettedSpriteShaderContext) {
			shaderContext.setPalette(palette)
			shaderContext.setAdjustmentMap(adjustmentMap)
			shaderContext.setAlphaMask(alphaMask)
		}
		sprite.render(shaderContext, globalTransform.set(parent.transform).mul(transform), framePosition)
	}

	/**
	 * Modify the transform of this component.
	 */
	SpriteComponent translate(float x, float y, float z) {

		transform.translate(x, y, z)
		return this
	}
}
