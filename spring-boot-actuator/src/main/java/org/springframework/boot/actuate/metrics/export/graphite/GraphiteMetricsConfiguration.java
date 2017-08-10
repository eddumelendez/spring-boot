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

package org.springframework.boot.actuate.metrics.export.graphite;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.graphite.GraphiteConfig;
import io.micrometer.core.instrument.graphite.GraphiteMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

/**
 * @version 2.0.0
 */
@Configuration
public class GraphiteMetricsConfiguration {
	@Bean
	public GraphiteMeterRegistry meterRegistry(GraphiteConfig config,
			HierarchicalNameMapper hierarchicalNameMapper, Clock clock) {
		return new GraphiteMeterRegistry(config, hierarchicalNameMapper, clock);
	}

	@Bean
	public GraphiteConfig graphiteConfig(Environment environment) {
		return environment::getProperty;
	}

	@ConditionalOnMissingBean
	@Bean
	public HierarchicalNameMapper hierarchicalNameMapper() {
		return HierarchicalNameMapper.DEFAULT;
	}

	@ConditionalOnMissingBean
	@Bean
	public Clock clock() {
		return Clock.SYSTEM;
	}
}
