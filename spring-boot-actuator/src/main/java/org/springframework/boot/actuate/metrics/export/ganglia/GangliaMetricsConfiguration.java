/**
 * Copyright 2017 Pivotal Software, Inc.
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
package org.springframework.boot.actuate.metrics.export.ganglia;

import io.micrometer.core.instrument.Clock;
import io.micrometer.core.instrument.ganglia.GangliaConfig;
import io.micrometer.core.instrument.ganglia.GangliaMeterRegistry;
import io.micrometer.core.instrument.util.HierarchicalNameMapper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class GangliaMetricsConfiguration {
	@Bean
	public GangliaMeterRegistry meterRegistry(GangliaConfig config,
			HierarchicalNameMapper hierarchicalNameMapper, Clock clock) {
		return new GangliaMeterRegistry(config, hierarchicalNameMapper, clock);
	}

	@Bean
	public GangliaConfig gangliaConfig(Environment environment) {
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
