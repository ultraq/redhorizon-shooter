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

import nz.net.ultraq.redhorizon.scenegraph.Node

/**
 * Any object in the scene that should be updated periodically.
 *
 * @author Emanuel Rabina
 */
class GameObject<T extends GameObject> extends Node<T> implements AutoCloseable {

	private List<Component> components = []

	/**
	 * Add a component to this object.
	 */
	T addComponent(Component component) {

		components << component
		return (T)this
	}

	@Override
	void close() {

		components.each { component ->
			if (component instanceof AutoCloseable) {
				component.close()
			}
		}
	}

	/**
	 * Return the first component that matches the given predicate.
	 */
	Component findComponent(Closure predicate) {

		return components.find(predicate)
	}

	/**
	 * Called regularly to perform any processing as a response to changes in the
	 * scene.
	 *
	 * @param delta
	 *   Time, in seconds, since the last time this method was called.
	 */
	void update(float delta) {

		components*.update(this, delta)
	}
}
