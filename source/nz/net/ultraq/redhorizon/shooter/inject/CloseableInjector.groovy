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

package nz.net.ultraq.redhorizon.shooter.inject

import com.google.inject.Binding
import com.google.inject.Injector
import com.google.inject.spi.DefaultElementVisitor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import groovy.transform.TupleConstructor

/**
 * Wraps an injector and closes all of its closeable bindings when closed.
 *
 * @author Emanuel Rabina
 */
@TupleConstructor(defaults = false)
class CloseableInjector implements AutoCloseable {

	private static final Logger logger = LoggerFactory.getLogger(CloseableInjector)

	@Delegate
	final Injector injector

	@Override
	void close() {

		logger.debug('Closing injected resources')

		injector.elements.each { element ->
			element.acceptVisitor(new DefaultElementVisitor<Void>() {
				@Override
				Void visit(Binding binding) {
					var object = binding.provider.get()
					if (object instanceof AutoCloseable) {
						logger.debug('Closing {}', object.class.simpleName)
						object.close()
					}
					return null
				}
			})
		}
	}
}
