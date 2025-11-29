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

import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Sprite
import nz.net.ultraq.redhorizon.graphics.SpriteSheet
import nz.net.ultraq.redhorizon.shooter.Player

import org.joml.Matrix4f
import org.joml.Vector2f

/**
 * A component for adding a {@link Sprite} to an entity.
 *
 * @author Emanuel Rabina
 */
class SpriteComponent<T extends Player> implements Component<T>, AutoCloseable {

	final String name
	final SpriteSheet spriteSheet
	private final Matrix4f transform = new Matrix4f()
	private final Sprite sprite
	private final Matrix4f spriteTransform = new Matrix4f()
	private final Vector2f framePosition = new Vector2f()
	private Palette palette
	private Shader<SceneShaderContext> shader

	/**
	 * Constructor, use the given sprite sheet for the sprite.
	 */
	SpriteComponent(String name, SpriteSheet spriteSheet) {

		this.name = name
		this.spriteSheet = spriteSheet
		sprite = new Sprite(spriteSheet)
	}

	@Override
	void close() {

		sprite.close()
	}

	/**
	 * Render the sprite.
	 */
	void render(GameObject gameObject, GraphicsContext context) {

		context.palettedSpriteShader.useShader { shaderContext ->
			context.camera().update(shaderContext)
			shaderContext.setAdjustmentMap(context.adjustmentMap())
			shaderContext.setAlphaMask(context.alphaMask())
			if (palette) {
				shaderContext.setPalette(palette)
			}
			sprite.draw(shaderContext, transform.mul(gameObject.transform, spriteTransform), framePosition)
		}
	}

	/**
	 * Modify the transform of this component.
	 */
	SpriteComponent translate(float x, float y, float z) {

		transform.translate(x, y, z)
		return this
	}

	@Override
	void update(T player, float delta) {

		// NOTE: C&C unit headings were ordered in a counter-clockwise order, the
		//       reverse from how degrees-based headings are done.
		var closestHeading = Math.round(player.heading / player.headingStep)
		var frame = closestHeading ? player.headings - closestHeading as int : 0
		if (player.accelerating) {
			frame += player.headings
		}
		framePosition.set(spriteSheet.getFramePosition(frame))
	}

	/**
	 * Set the shader to use for the sprite.
	 */
	SpriteComponent withShader(Shader<SceneShaderContext> shader) {

		this.shader = shader
		return this
	}

	/**
	 * Set the palette to use for the sprite.
	 */
	SpriteComponent withPalette(Palette palette) {

		this.palette = palette
		return this
	}
}
