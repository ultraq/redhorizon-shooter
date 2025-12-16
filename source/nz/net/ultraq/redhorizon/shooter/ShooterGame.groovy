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

import nz.net.ultraq.redhorizon.audio.AudioDevice
import nz.net.ultraq.redhorizon.audio.openal.OpenALAudioDevice
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.shooter.engine.ScriptEngine

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command
import static org.lwjgl.glfw.GLFW.*

/**
 * Entry point to the Shooter game.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'shooter')
class ShooterGame implements Runnable {

	static {
		System.setProperty('joml.format', 'false')
		System.setProperty('org.lwjgl.system.stackSize', '10240')
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new ShooterGame()).execute(args))
	}

	private static final Logger logger = LoggerFactory.getLogger(ShooterGame)
	private static final int WINDOW_WIDTH = 640
	private static final int WINDOW_HEIGHT = 400

	private Window window
	private InputEventHandler inputEventHandler
	private AudioDevice audioDevice
	private ScriptEngine scriptEngine
	private ShooterScene scene

	@Override
	void run() {

		try {
			// Startup
			logger.debug('Setup')
			var properties = new Properties()
			properties.load(getResourceAsStream('shooter.properties'))

			// Init devices
			window = new OpenGLWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Shooter ${properties.getProperty('version')}")
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.GREY)
				.withVSync(true)
			inputEventHandler = new InputEventHandler()
				.addInputSource(window)
			audioDevice = new OpenALAudioDevice()
				.withMasterVolume(0.5f)
			scriptEngine = new ScriptEngine('.')

			// Init scene
			scene = new ShooterScene(WINDOW_WIDTH, WINDOW_HEIGHT, window, inputEventHandler, scriptEngine)

			// Game loop
			logger.debug('Game loop')
			window
				.addImGuiComponent(new DebugOverlay()
					.withCursorTracking(scene.camera.camera, scene.camera.transform))
				.addImGuiComponent(new NodeList(scene))
				.show()

			var deltaTimer = new DeltaTimer()
			while (!window.shouldClose()) {
				var delta = deltaTimer.deltaTime()

				logic(delta)
				render()

				Thread.yield()
			}
		}
		finally {
			// Shutdown
			logger.debug('Shutdown')
			scene?.close()
			audioDevice?.close()
			window?.close()
		}
	}

	/**
	 * Perform the game logic.
	 */
	private void logic(float delta) {

		// Game-wide input events
		if (inputEventHandler.keyPressed(GLFW_KEY_ESCAPE, true)) {
			window.shouldClose(true)
		}
		if (inputEventHandler.keyPressed(GLFW_KEY_I, true)) {
			window.toggleImGuiWindows()
		}
		if (inputEventHandler.keyPressed(GLFW_KEY_V, true)) {
			window.toggleVSync()
		}

		scene.update(delta)
	}

	/**
	 * Draw game objects to the screen and keep audio streams running.
	 */
	private void render() {

		window.useWindow { ->
			scene.render()
		}
	}
}
