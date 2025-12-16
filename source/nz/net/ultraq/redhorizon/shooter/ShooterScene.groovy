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

import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.engine.ScriptEngine
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Palette
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.shooter.engine.CameraObject
import nz.net.ultraq.redhorizon.shooter.engine.GameObject
import nz.net.ultraq.redhorizon.shooter.engine.GraphicsComponent
import nz.net.ultraq.redhorizon.shooter.utilities.GridLines

import org.joml.primitives.Rectanglef

/**
 * Set up and update via scripts the scene of the game.
 *
 * @author Emanuel Rabina
 */
class ShooterScene extends Scene implements AutoCloseable {

	final CameraObject camera
	final GridLines gridLines
	final Palette palette
	final Player player

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

		var basicShader = new BasicShader()
		shaders.addAll(basicShader, new ShadowShader(), new PalettedSpriteShader())

		camera = new CameraObject(sceneWidth, sceneHeight, window)
		gridLines = new GridLines(new Rectanglef(0, 0, sceneWidth, sceneHeight).center(), 24f)
		palette = resourceManager.loadPalette('temperat-td.pal')
		player = new Player(sceneWidth, sceneHeight, resourceManager, palette, scriptEngine, inputEventHandler)

		addChild(camera)
		addChild(gridLines)
		addChild(player)
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
			if (node instanceof GameObject) {
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

		traverse { node ->
			if (node instanceof GameObject) {
				node.update(delta)
			}
		}
	}
}
