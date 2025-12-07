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

import nz.net.ultraq.redhorizon.classic.graphics.AlphaMask
import nz.net.ultraq.redhorizon.classic.graphics.FactionAdjustmentMap
import nz.net.ultraq.redhorizon.engine.ScriptEngine
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.shooter.engine.GameObject
import nz.net.ultraq.redhorizon.shooter.objects.CameraObject
import nz.net.ultraq.redhorizon.shooter.utilities.GridLines
import nz.net.ultraq.redhorizon.shooter.utilities.ShaderManager

import org.joml.primitives.Rectanglef

/**
 * Set up and update via scripts the scene of the game.
 *
 * @author Emanuel Rabina
 */
class ShooterScene extends Scene implements AutoCloseable {

	final CameraObject cameraObject
	final GridLines gridLines
	final Player player

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	ShooterScene(int sceneWidth, int sceneHeight, Window window, ResourceManager resourceManager, ShaderManager shaderManager,
		FactionAdjustmentMap adjustmentMap, AlphaMask alphaMask, ScriptEngine scriptEngine, InputEventHandler inputEventHandler) {

		cameraObject = new CameraObject(sceneWidth, sceneHeight, window)
		gridLines = new GridLines(new Rectanglef(0, 0, sceneWidth, sceneHeight).center(), 24f, camera, shaderManager.basicShader)
		player = new Player(resourceManager, shaderManager, resourceManager.loadPalette('temperat-td.pal'), adjustmentMap,
			alphaMask, scriptEngine, camera, inputEventHandler)

		addChild(cameraObject)
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
	}

	/**
	 * Locate this scene's camera.
	 */
	Camera getCamera() {

		return cameraObject.camera
	}

	/**
	 * Draw out all the graphical components of the scene.
	 */
	void render() {

		traverse { node ->
			if (node instanceof GameObject) {
				node.render()
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
