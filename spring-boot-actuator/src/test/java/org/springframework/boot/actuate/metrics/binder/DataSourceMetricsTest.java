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

package org.springframework.boot.actuate.metrics.binder;

import java.sql.SQLException;
import java.util.Collection;

import javax.sql.DataSource;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.SpringBootTestApplication;
import org.springframework.boot.actuate.metrics.SpringMeters;
import org.springframework.boot.actuate.metrics.export.prometheus.EnablePrometheusMetrics;
import org.springframework.boot.autoconfigure.jdbc.metadata.DataSourcePoolMetadataProvider;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Jon Schneider
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = { "spring.datasource.generate-unique-name=true",
		"management.security.enabled=false" })
public class DataSourceMetricsTest {

	@Autowired
	private DataSource dataSource;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void dataSourceIsInstrumented() throws SQLException, InterruptedException {
		this.dataSource.getConnection().getMetaData();
		String scrape = this.restTemplate.getForObject("/prometheus", String.class);
		System.out.println(scrape);
	}

	@SpringBootTestApplication
	@EnablePrometheusMetrics
	@Import(DataSourceConfig.class)
	static class MetricsApp {
		public static void main(String[] args) {
			SpringApplication.run(MetricsApp.class, "--debug");
		}
	}

	@Configuration
	static class DataSourceConfig {
		public DataSourceConfig(DataSource dataSource,
				Collection<DataSourcePoolMetadataProvider> metadataProviders,
				MeterRegistry registry) {
			SpringMeters.monitor(registry, dataSource, metadataProviders, "data_source");
		}
	}
}
