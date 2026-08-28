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

import nz.net.ultraq.redhorizon.classic.graphics.ShadowShader
import nz.net.ultraq.redhorizon.engine.graphics.GridLines
import nz.net.ultraq.redhorizon.graphics.Colour
import nz.net.ultraq.redhorizon.graphics.PaletteAlphaMask
import nz.net.ultraq.redhorizon.graphics.opengl.PalettedSpriteShader
import nz.net.ultraq.redhorizon.runtime.Application
import nz.net.ultraq.redhorizon.runtime.Runtime
import nz.net.ultraq.redhorizon.runtime.objects.ScreenEdges
import nz.net.ultraq.redhorizon.runtime.utilities.VersionReader
import nz.net.ultraq.redhorizon.scenegraph.Node
import nz.net.ultraq.redhorizon.scenegraph.Scene
import static nz.net.ultraq.redhorizon.runtime.ScopedValues.RESOURCE_MANAGER

import org.joml.primitives.Rectanglef
import picocli.CommandLine
import picocli.CommandLine.Command

import java.util.concurrent.Callable

/**
 * Entry point to the Shooter game.
 *
 * @author Emanuel Rabina
 */
@Command(name = 'shooter')
class Shooter extends Application implements Callable<Integer> {

	public static final int WINDOW_WIDTH = 640
	public static final int WINDOW_HEIGHT = 480
	public static final Rectanglef WINDOW_BOUNDS = new Rectanglef(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT).center()

	static {
		System.setProperty('joml.format', 'false')
	}

	static void main(String[] args) {
		System.exit(new CommandLine(new Shooter()).execute(args))
	}

	Shooter() {

		super('Shooter', new VersionReader('shooter.properties').read())
	}

	@Override
	Integer call() {

		return new Runtime(this)
			.withWindowBackgroundColour(Colour.GREY)
			.withWindowWidth(WINDOW_WIDTH)
			.withWindowHeight(WINDOW_HEIGHT)
			.withCameraWidth(WINDOW_WIDTH)
			.withCameraHeight(WINDOW_HEIGHT)
			.withAdditionalShaders { -> [new ShadowShader(), new PalettedSpriteShader()] }
			.withAudioListenerGain(0.5f)
			.withGridLines { ->
				return new GridLines(new Rectanglef(0, 0, WINDOW_WIDTH, WINDOW_HEIGHT).center(), 24f,
					new Colour('GridLines-Origin', 0.2f, 0.2f, 0.2f), new Colour('GridLines-Dividers', 0.6f, 0.6f, 0.6f))
			}
			.execute()
	}

	@Override
	Scene configureScene(Scene scene) {

		var resourceManager = RESOURCE_MANAGER.get()
		return scene
			.addChild(new PaletteAlphaMask()
				.withName('Alpha mask (RA)'))
			.addChild(new Player())
			.addChild(new ScreenEdges(WINDOW_BOUNDS))
			.addChild(new Node()
				.withName('Bullets')
				.addChild(resourceManager.loadPalette('temperat-ra.pal')
					.withName('Palette (RA)')))
	}
}
