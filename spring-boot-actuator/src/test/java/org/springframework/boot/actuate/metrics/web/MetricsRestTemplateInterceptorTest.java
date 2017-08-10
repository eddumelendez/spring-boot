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

package org.springframework.boot.actuate.metrics.web;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.EnableMetrics;
import org.springframework.boot.actuate.metrics.SpringBootTestApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.client.match.MockRestRequestMatchers;
import org.springframework.test.web.client.response.MockRestResponseCreators;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Jon Schneider
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = MetricsRestTemplateInterceptorTest.App.class)
public class MetricsRestTemplateInterceptorTest {
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private MeterRegistry registry;

	@Test
	public void interceptRestTemplate() {
		MockRestServiceServer mockServer = MockRestServiceServer
				.createServer(this.restTemplate);
		mockServer
				.expect(MockRestRequestMatchers.requestTo("/test/123"))
				.andExpect(MockRestRequestMatchers.method(HttpMethod.GET))
				.andRespond(
						MockRestResponseCreators.withSuccess("OK",
								MediaType.APPLICATION_JSON));

		String s = this.restTemplate.getForObject("/test/{id}", String.class, 123);

		// the uri requires AOP to determine
		assertThat(
				this.registry.findMeter(Timer.class, "http_client_requests", "method", "GET",
						"uri", "/test/{id}", "status", "200")).containsInstanceOf(
				Timer.class).hasValueSatisfying(t -> assertThat(t.count()).isEqualTo(1));

		assertThat(s).isEqualTo("OK");

		mockServer.verify();
	}

	@SpringBootTestApplication
	@EnableMetrics
	static class App {
		@Bean
		MeterRegistry meterRegistry() {
			return new SimpleMeterRegistry();
		}

		@Bean
		RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}
}
