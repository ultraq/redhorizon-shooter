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

import nz.net.ultraq.redhorizon.classic.graphics.AlphaMaskComponent
import nz.net.ultraq.redhorizon.classic.graphics.PaletteComponent
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.CameraEntity
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsComponent
import nz.net.ultraq.redhorizon.engine.scripts.GameLogicComponent
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.shooter.utilities.GridLines

import org.joml.primitives.Rectanglef

/**
 * Set up and update via scripts the scene of the game.
 *
 * @author Emanuel Rabina
 */
class ShooterScene extends Scene implements AutoCloseable {

	final CameraEntity camera
	private final ResourceManager resourceManager

	/**
	 * A collection of shaders in this manager in the order in which objects in
	 * the scene should be grouped for rendering.
	 */
	private final List<Shader<? extends SceneShaderContext>> shaders = new ArrayList<>()

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	ShooterScene(int sceneWidth, int sceneHeight, Window window, InputEventHandler inputEventHandler,
		ScriptEngine scriptEngine) {

		resourceManager = new ResourceManager('nz/net/ultraq/redhorizon/shooter/')
		shaders.addAll(new BasicShader(), new ShadowShader(), new PalettedSpriteShader())
		camera = new CameraEntity(sceneWidth, sceneHeight, window)

		addChild(camera)
		addChild(new GridLines(new Rectanglef(0, 0, sceneWidth, sceneHeight).center(), 24f))
		addChild(new Entity()
			.addComponent(new PaletteComponent(resourceManager.loadPalette('temperat-td.pal')))
			.addComponent(new AlphaMaskComponent())
			.withName('Palette & alpha mask'))
		addChild(new Player(sceneWidth, sceneHeight, resourceManager, scriptEngine, inputEventHandler))
	}

	@Override
	void close() {

		traverse { node ->
			if (node instanceof AutoCloseable) {
				node.close()
			}
		}
		shaders*.close()
		resourceManager.close()
	}

	/**
	 * Draw out all the graphical components of the scene.
	 */
	void render() {

		var graphicsComponents = new ArrayList<GraphicsComponent>()
		traverse { node ->
			if (node instanceof Entity) {
				// TODO: Create an allocation-free method of finding objects components
				graphicsComponents.addAll(node.findComponents { it instanceof GraphicsComponent })
			}
		}
		// TODO: Create an allocation-free method of grouping objects
		var groupedComponents = graphicsComponents.groupBy { it.shaderClass }

		shaders.each { shader ->
			shader.useShader { shaderContext ->
				camera.render(shaderContext)
				groupedComponents[shader.class].each { component ->
					component.render(shaderContext)
				}
			}
		}
	}

	/**
	 * Perform a scene update in the game loop.
	 */
	void update(float delta) {

		// TODO: Similar to above, these look like they should be the "S" part of ECS
		var gameLogicComponents = new ArrayList<GameLogicComponent>()
		traverse { node ->
			if (node instanceof Entity) {
				gameLogicComponents.addAll(node.findComponents { it instanceof GameLogicComponent })
			}
		}
		gameLogicComponents.each { component ->
			component.update(delta)
		}
	}
}
