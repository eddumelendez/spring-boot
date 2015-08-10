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

package org.springframework.boot.autoconfigure.cache;

import com.gemstone.gemfire.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.gemfire.support.GemfireCacheManager;

/**
 * @author Eddú Meléndez
 * @since 1.3.0
 */
@Configuration
@ConditionalOnClass({Cache.class, GemfireCacheManager.class})
@ConditionalOnMissingBean(CacheManager.class)
@Conditional(CacheCondition.class)
public class GemfireCacheConfiguration {

	@Autowired
	private CacheProperties cacheProperties;

	@Bean
	private GemfireCacheManager cacheManager() {
		return new GemfireCacheManager();
	}
}
