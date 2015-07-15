package org.springframework.boot.autoconfigure.cassandra;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import com.datastax.driver.core.Session;
import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.cassandra.core.CassandraTemplate;

/**
 * Tests for {@link CassandraDataAutoConfiguration}
 *
 * @author Eddú Meléndez
 */
public class CassandraDataAutoConfigurationTests {

	private AnnotationConfigApplicationContext context;

	@After
	public void close() {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void templateExists() {
		this.context = new AnnotationConfigApplicationContext();
		this.context.register(TestExcludeConfiguration.class,
				TestConfiguration.class,
				PropertyPlaceholderAutoConfiguration.class,
				CassandraAutoConfiguration.class,
				CassandraDataAutoConfiguration.class);
		this.context.refresh();
		assertEquals(1,
				this.context.getBeanNamesForType(CassandraTemplate.class).length);
	}

	@Configuration
	@ComponentScan(excludeFilters = @ComponentScan.Filter(classes = {
			Session.class }, type = FilterType.ASSIGNABLE_TYPE) )
	static class TestExcludeConfiguration {

	}

	@Configuration
	static class TestConfiguration {

		@Bean
		public Session getObject() {
			return mock(Session.class);
		}

	}

}
