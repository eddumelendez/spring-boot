/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.metrics;

import java.util.Collection;

import javax.annotation.PostConstruct;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Configuration;

/**
 * Post-construction setup of a meter registry, regardless of its backing implementation.
 *
 * @author Jon Schneider
 * @version 2.0.0
 */
@Configuration
public class MeterRegistryConfigurationSupport {

	private final MeterRegistry registry;

	private final Collection<MeterBinder> binders;

	private final Collection<MeterRegistryConfigurer> registryConfigurers;

	public MeterRegistryConfigurationSupport(MeterRegistry registry,
			ObjectProvider<Collection<MeterBinder>> binders,
			ObjectProvider<Collection<MeterRegistryConfigurer>> registryConfigurers) {
		this.registry = registry;
		this.binders = binders.getIfAvailable();
		this.registryConfigurers = registryConfigurers.getIfAvailable();
	}

	@PostConstruct
	void bindAll() {
		// Important that this happens before binders are applied, as it
		// may involve adding common tags that should apply to metrics registered
		// in those binders.
		if (this.registryConfigurers != null) {
			this.registryConfigurers.forEach(conf -> conf
					.configureRegistry(this.registry));
		}

		if (this.binders != null) {
			this.binders.forEach(binder -> binder.bindTo(this.registry));
		}
	}
}
