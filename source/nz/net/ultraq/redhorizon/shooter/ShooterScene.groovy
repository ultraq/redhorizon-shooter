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

import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.shooter.engine.CameraComponent
import nz.net.ultraq.redhorizon.shooter.engine.GameObject
import nz.net.ultraq.redhorizon.shooter.engine.ScriptEngine
import nz.net.ultraq.redhorizon.shooter.objects.CameraObject
import nz.net.ultraq.redhorizon.shooter.utilities.GridLines
import nz.net.ultraq.redhorizon.shooter.utilities.ResourceManager

import org.joml.primitives.Rectanglef

/**
 * Set up and update via scripts the scene of the game.
 *
 * @author Emanuel Rabina
 */
class ShooterScene extends Scene implements AutoCloseable {

	private final CameraObject cameraObject
	final GridLines gridLines
	final Player player

	/**
	 * Constructor, create a new scene to the given dimensions.
	 */
	ShooterScene(int sceneWidth, int sceneHeight, Window window, ResourceManager resourceManager, ScriptEngine scriptEngine,
		InputEventHandler inputEventHandler) {

		cameraObject = new CameraObject(sceneWidth, sceneHeight, window)
		gridLines = new GridLines(new Rectanglef(0, 0, sceneWidth, sceneHeight).center(), 24f)
		player = new Player(resourceManager, scriptEngine, cameraObject.camera, inputEventHandler)

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

		return ((CameraComponent)cameraObject.findComponent { it instanceof CameraComponent }).camera
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
