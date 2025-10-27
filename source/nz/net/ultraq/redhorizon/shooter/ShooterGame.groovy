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
import nz.net.ultraq.redhorizon.classic.Faction
import nz.net.ultraq.redhorizon.classic.graphics.AlphaMask
import nz.net.ultraq.redhorizon.classic.graphics.FactionAdjustmentMap
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.graphics.Camera
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.scenegraph.Scene
import nz.net.ultraq.redhorizon.shooter.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.shooter.utilities.GridLines
import nz.net.ultraq.redhorizon.shooter.utilities.ResourceManager

import org.joml.primitives.Rectanglef
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
		System.setProperty('org.lwjgl.system.stackSize', '10240')
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new ShooterGame()).execute(args))
	}

	private static final Logger logger = LoggerFactory.getLogger(ShooterGame)
	private static final int WINDOW_WIDTH = 640
	private static final int WINDOW_HEIGHT = 400

	private Scene scene
	private Window window
	private Camera camera
	private InputEventHandler inputEventHandler
	private BasicShader basicShader
	private PalettedSpriteShader palettedSpriteShader
	private FactionAdjustmentMap adjustmentMap
	private AlphaMask alphaMask
	private AudioDevice audioDevice

	private ResourceManager resourceManager
	private Player player
	private GridLines gridLines

	@Override
	void run() {

		try {
			// Startup
			logger.debug('Setup')
			var properties = new Properties()
			properties.load(getResourceAsStream('shooter.properties'))

			// Init devices
			scene = new Scene()
			window = new OpenGLWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "Shooter ${properties.getProperty('version')}")
				.addFpsCounter()
				.addNodeList(scene)
				.centerToScreen()
				.scaleToFit()
				.withBackgroundColour(Colour.GREY)
				.withVSync(true)
			camera = new Camera(WINDOW_WIDTH, WINDOW_HEIGHT, window)
			scene << camera
			inputEventHandler = new InputEventHandler()
				.addInputSource(window)
			basicShader = new BasicShader()
			palettedSpriteShader = new PalettedSpriteShader()
			adjustmentMap = new FactionAdjustmentMap(Faction.GOLD)
			alphaMask = new AlphaMask()

			audioDevice = new OpenALAudioDevice()
				.withMasterVolume(0.5f)

			// Init game assets
			resourceManager = new ResourceManager('nz/net/ultraq/redhorizon/shooter/')
			player = new Player(resourceManager)
			scene << player
			gridLines = new GridLines(new Rectanglef(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT).center(), 24f)
			scene << gridLines

			// Game loop
			logger.debug('Game loop')
			window.show()
			var deltaTimer = new DeltaTimer()
			while (!window.shouldClose()) {
				var delta = deltaTimer.deltaTime()

				input(delta)
				logic(delta)
				render()

				Thread.yield()
			}
		}
		finally {
			// Shutdown
			logger.debug('Shutdown')
			scene?.traverse { node ->
				if (node instanceof AutoCloseable) {
					node.close()
				}
			}
			resourceManager?.close()
			audioDevice?.close()
			alphaMask?.close()
			adjustmentMap?.close()
			palettedSpriteShader?.close()
			basicShader?.close()
			window?.close()
		}
	}

	/**
	 * Process input events.
	 */
	private void input(float delta) {

		if (inputEventHandler.keyPressed(GLFW_KEY_ESCAPE, true)) {
			window.shouldClose(true)
		}
		if (inputEventHandler.keyPressed(GLFW_KEY_I, true)) {
			window.toggleImGuiWindows()
		}
		if (inputEventHandler.keyPressed(GLFW_KEY_V, true)) {
			window.toggleVSync()
		}
	}

	/**
	 * Perform the game logic.
	 */
	private void logic(float delta) {

		scene.traverse { node ->
			if (node instanceof GameObject) {
				node.update(delta)
			}
		}
	}

	/**
	 * Draw game objects to the screen and keep audio streams running.
	 */
	private void render() {

		window.useWindow { ->
			basicShader.useShader { shaderContext ->
				camera.update(shaderContext)
				gridLines.draw(shaderContext)
			}
			palettedSpriteShader.useShader { shaderContext ->
				camera.update(shaderContext)
				shaderContext.setAdjustmentMap(adjustmentMap)
				adjustmentMap.update()
				shaderContext.setAlphaMask(alphaMask)
				player.render(shaderContext)
			}
		}
	}
}
