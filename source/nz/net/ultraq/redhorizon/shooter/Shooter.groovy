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
import nz.net.ultraq.redhorizon.classic.graphics.PalettedSpriteShader
import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.engine.Engine
import nz.net.ultraq.redhorizon.engine.Entity
import nz.net.ultraq.redhorizon.engine.graphics.GraphicsSystem
import nz.net.ultraq.redhorizon.engine.graphics.imgui.ImGuiDebugComponent
import nz.net.ultraq.redhorizon.engine.graphics.imgui.LogPanel
import nz.net.ultraq.redhorizon.engine.graphics.imgui.NodeList
import nz.net.ultraq.redhorizon.engine.input.InputSystem
import nz.net.ultraq.redhorizon.engine.physics.CollisionSystem
import nz.net.ultraq.redhorizon.engine.scene.SceneChangesSystem
import nz.net.ultraq.redhorizon.engine.scripts.ScriptEngine
import nz.net.ultraq.redhorizon.engine.scripts.ScriptSystem
import nz.net.ultraq.redhorizon.engine.utilities.DeltaTimer
import nz.net.ultraq.redhorizon.engine.utilities.ResourceManager
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.Framebuffer
import nz.net.ultraq.redhorizon.graphics.SceneShaderContext
import nz.net.ultraq.redhorizon.graphics.Shader
import nz.net.ultraq.redhorizon.graphics.Window
import nz.net.ultraq.redhorizon.graphics.imgui.DebugOverlay
import nz.net.ultraq.redhorizon.graphics.opengl.BasicShader
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLFramebuffer
import nz.net.ultraq.redhorizon.graphics.opengl.OpenGLWindow
import nz.net.ultraq.redhorizon.input.InputEventHandler
import nz.net.ultraq.redhorizon.shooter.debug.DebugCollisionOutlineSystem
import nz.net.ultraq.redhorizon.shooter.debug.DebugEverythingBinding
import nz.net.ultraq.redhorizon.shooter.debug.DebugLinesBinding

import org.lwjgl.system.Configuration
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import picocli.CommandLine
import picocli.CommandLine.Command

/**
 * Entry point to the Shooter game.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'shooter')
class Shooter implements Runnable {

	static {
		System.setProperty('joml.format', 'false')
		Configuration.STACK_SIZE.set(10240)
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new Shooter()).execute(args))
	}

	private static final Logger logger = LoggerFactory.getLogger(Shooter)
	private static final int WINDOW_WIDTH = 640
	private static final int WINDOW_HEIGHT = 400

	private Window window
	private Framebuffer framebuffer
	private Shader<SceneShaderContext>[] shaders
	private AudioDevice audioDevice
	private ResourceManager resourceManager
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
			framebuffer = new OpenGLFramebuffer(WINDOW_WIDTH, WINDOW_HEIGHT)
			shaders = [new BasicShader(), new ShadowShader(), new PalettedSpriteShader()]
			audioDevice = new OpenALAudioDevice()
				.withMasterVolume(0.5f)
			resourceManager = new ResourceManager('nz/net/ultraq/redhorizon/shooter/')
			var inputEventHandler = new InputEventHandler()
				.addInputSource(window)
				.addEscapeToCloseBinding(window)
				.addVSyncBinding(window)

			ScopedValue
				.where(ScopedValues.WINDOW, window)
				.where(ScopedValues.RESOURCE_MANAGER, resourceManager)
				.run { ->

					// Init scene
					scene = new ShooterScene(WINDOW_WIDTH, WINDOW_HEIGHT, window).tap {
						var debugOverlayComponent = new ImGuiDebugComponent(new DebugOverlay()
							.withCursorTracking(camera.camera, camera.transform, this.window)).disable()
						var nodeListComponent = new ImGuiDebugComponent(new NodeList(it)).disable()
						var logPanelComponent = new ImGuiDebugComponent(new LogPanel()).disable()
						addChild(new Entity()
							.addComponent(debugOverlayComponent)
							.addComponent(nodeListComponent)
							.addComponent(logPanelComponent)
							.withName('Debug UI'))

						var debugLinesBinding = new DebugLinesBinding(it)
						var debugEverythingBinding = new DebugEverythingBinding(
							[debugOverlayComponent, nodeListComponent, logPanelComponent], debugLinesBinding)
						inputEventHandler
							.addImGuiDebugBindings([debugOverlayComponent], [nodeListComponent, logPanelComponent])
							.addInputBinding(debugLinesBinding)
							.addInputBinding(debugEverythingBinding)
					}
					var engine = new Engine()
						.addSystem(new InputSystem(inputEventHandler))
						.addSystem(new DebugCollisionOutlineSystem())
						.addSystem(new ScriptSystem(new ScriptEngine('source/nz/net/ultraq/redhorizon/shooter/'), inputEventHandler))
						.addSystem(new CollisionSystem())
						.addSystem(new GraphicsSystem(window, framebuffer, shaders))
						.addSystem(new SceneChangesSystem())
						.withScene(scene)

					// Game loop
					logger.debug('Game loop')
					window.show()
					var deltaTimer = new DeltaTimer()
					while (!window.shouldClose()) {
						engine.update(deltaTimer.deltaTime())
						Thread.yield()
					}
				}
		}
		finally {
			// Shutdown
			logger.debug('Shutdown')
			scene?.close()
			resourceManager?.close()
			audioDevice?.close()
			shaders?.each { it.close() }
			framebuffer?.close()
			window?.close()
		}
	}
}
