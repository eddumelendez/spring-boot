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

import java.util.ArrayList;
import java.util.List;

import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.actuate.metrics.web.MetricsRestTemplateInterceptor;
import org.springframework.boot.actuate.metrics.web.RestTemplateTagConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass(name = "org.springframework.web.client.RestTemplate")
class RestTemplateMetricsConfiguration {

	private final RestTemplateTagConfigurer tagConfigurer;

	public RestTemplateMetricsConfiguration(
			ObjectProvider<RestTemplateTagConfigurer> tagConfigurer) {
		this.tagConfigurer = tagConfigurer.getIfAvailable();
	}

	@Bean
	@ConditionalOnMissingBean(RestTemplateTagConfigurer.class)
	public RestTemplateTagConfigurer restTemplateTagConfigurer() {
		if (this.tagConfigurer != null) {
			return this.tagConfigurer;
		}
		return new RestTemplateTagConfigurer();
	}

	@Bean
	public MetricsRestTemplateInterceptor clientHttpRequestInterceptor(
			MeterRegistry meterRegistry,
			RestTemplateTagConfigurer restTemplateTagConfigurer, Environment environment) {
		return new MetricsRestTemplateInterceptor(meterRegistry,
				restTemplateTagConfigurer,
				environment.getProperty("spring.metrics.web.client_requests.name",
						"http_client_requests"));
	}

	@Bean
	public static BeanPostProcessor restTemplateInterceptorPostProcessor() {
		return new MetricsInterceptorPostProcessor();
	}

	private static class MetricsInterceptorPostProcessor implements BeanPostProcessor,
			ApplicationContextAware {
		private ApplicationContext context;
		private MetricsRestTemplateInterceptor interceptor;

		@Override
		public Object postProcessBeforeInitialization(Object bean, String beanName) {
			return bean;
		}

		@Override
		public Object postProcessAfterInitialization(Object bean, String beanName) {
			if (bean instanceof RestTemplate) {
				if (this.interceptor == null) {
					this.interceptor = this.context
							.getBean(MetricsRestTemplateInterceptor.class);
				}
				RestTemplate restTemplate = (RestTemplate) bean;
				// create a new list as the old one may be unmodifiable (ie
				// Arrays.asList())
				List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
				interceptors.add(this.interceptor);
				interceptors.addAll(restTemplate.getInterceptors());
				restTemplate.setInterceptors(interceptors);
			}
			return bean;
		}

		@Override
		public void setApplicationContext(ApplicationContext context)
				throws BeansException {
			this.context = context;
		}
	}
}
