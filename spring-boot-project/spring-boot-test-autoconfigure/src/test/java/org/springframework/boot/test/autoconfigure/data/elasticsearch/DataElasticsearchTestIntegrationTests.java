/*
 * Copyright 2012-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.test.autoconfigure.data.elasticsearch;

import java.time.Duration;
import java.util.UUID;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.elasticsearch.RestClientBuilderCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testsupport.testcontainers.DockerImageNames;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Sample test for {@link DataElasticsearchTest @DataElasticsearchTest}
 *
 * @author Eddú Meléndez
 * @author Moritz Halbritter
 * @author Andy Wilkinson
 * @author Phillip Webb
 */
@DataElasticsearchTest(properties = {
//		"spring.elasticsearch.restclient.ssl.bundle=elastic",
//		"spring.ssl.bundle.pem.elastic.keystore.certificate=classpath:ssl/ca.crt",
//		"spring.ssl.bundle.pem.elastic.truststore.private-key=classpath:ssl/ca.key",
//		"spring.ssl.bundle.pem.elastic.truststore.certificate=classpath:ssl/http_ca.crt",
//		"spring.ssl.bundle.pem.elastic.key.alias:ca", "spring.ssl.bundle.pem.elastic.truststore.type:pkcs12",
//		"spring.ssl.bundle.pem.elastic.protocol:TLSv1.3"
})
@Testcontainers(disabledWithoutDocker = true)
class DataElasticsearchTestIntegrationTests {

	@Container
	@ServiceConnection
	static final ElasticsearchContainer elasticsearch = new ElasticsearchContainer(DockerImageNames.elasticsearch())
		.withEnv("ES_JAVA_OPTS", "-Xms32m -Xmx512m")
		 .withStartupAttempts(5)
		 .withStartupTimeout(Duration.ofMinutes(10));

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Autowired
	private ExampleRepository exampleRepository;

	@Autowired
	private ApplicationContext applicationContext;

//	 @BeforeAll
//	 static void beforeAll() {
//		 elasticsearch.copyFileFromContainer("/usr/share/elasticsearch/config/certs/http_ca.crt",
//				 "src/test/resources/ssl/http_ca.crt");
//		 elasticsearch.copyFileFromContainer("/usr/share/elasticsearch/config/certs/http.p12",
//				 "src/test/resources/ssl/http.p12");
//	 }

	@Test
	void didNotInjectExampleService() {
		assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
			.isThrownBy(() -> this.applicationContext.getBean(ExampleService.class));
	}

	@Test
	void testRepository() {
		ExampleDocument document = new ExampleDocument();
		document.setText("Look, new @DataElasticsearchTest!");
		String id = UUID.randomUUID().toString();
		document.setId(id);
		ExampleDocument savedDocument = this.exampleRepository.save(document);
		ExampleDocument getDocument = this.elasticsearchTemplate.get(id, ExampleDocument.class);
		assertThat(getDocument).isNotNull();
		assertThat(getDocument.getId()).isNotNull();
		assertThat(getDocument.getId()).isEqualTo(savedDocument.getId());
		this.exampleRepository.deleteAll();
	}

	@TestConfiguration
	static class SSL {

		 @Bean
		 RestClientBuilderCustomizer customizer() {
			 return new RestClientBuilderCustomizer() {
				 @Override
				 public void customize(RestClientBuilder builder) {

				 }

				 @Override
				 public void customize(HttpAsyncClientBuilder builder) {
					 builder.setSSLContext(elasticsearch.createSslContextFromCa());
				 }
			 };
		 }

	}

}
