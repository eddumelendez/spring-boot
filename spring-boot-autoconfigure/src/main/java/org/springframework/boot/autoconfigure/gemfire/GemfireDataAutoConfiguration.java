/*
 * Copyright 2002-2015 the original author or authors.
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

package org.springframework.boot.autoconfigure.gemfire;

import com.gemstone.gemfire.cache.Region;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.GemfireTemplate;

/**
 * @author Eddú Meléndez
 */
@Configuration
@ConditionalOnClass({ Region.class, GemfireTemplate.class })
@AutoConfigureAfter(GemfireAutoConfiguration.class)
public class GemfireDataAutoConfiguration {

	@Bean
	@ConditionalOnMissingBean
	public GemfireTemplate gemfireTemplate(Region<?, ?> region) {
		return new GemfireTemplate(region);
	}

}
