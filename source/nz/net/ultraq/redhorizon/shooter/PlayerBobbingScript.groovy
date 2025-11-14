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

import nz.net.ultraq.redhorizon.shooter.engine.GameObjectScript

/**
 * A script for controlling the player sprite's bobbing motion.
 *
 * @author Emanuel Rabina
 */
class PlayerBobbingScript extends GameObjectScript {

	private float getBobbingTimer() {
		if (!binding.hasVariable('bobbingTimer')) {
			binding['bobbingTimer'] = 0f
		}
		return binding['bobbingTimer'] as float
	}

	private void setBobbingTimer(float bobbingTimer) {
		binding['bobbingTimer'] = bobbingTimer
	}

	@Override
	Object run() {

		bobbingTimer += delta
		return 0.0625f * Math.sin(bobbingTimer) as float
	}
}
