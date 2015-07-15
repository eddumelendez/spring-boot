package org.springframework.boot.autoconfigure.cassandra;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import org.junit.After;
import org.junit.Test;
import org.springframework.boot.autoconfigure.PropertyPlaceholderAutoConfiguration;
import org.springframework.boot.test.EnvironmentTestUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.datastax.driver.core.Cluster;

/**
 * Tests for {@link CassandraAutoConfiguration}
 *
 * @author Eddú Meléndez
 */
public class CassandraAutoConfigurationTest {

	private AnnotationConfigApplicationContext context;

	@After
	public void tearDown() throws Exception {
		if (this.context != null) {
			this.context.close();
		}
	}

	@Test
	public void createClusterWithDefault() {
		this.context = doLoad();
		assertEquals(1, this.context.getBeanNamesForType(Cluster.class).length);
		Cluster cluster = this.context.getBean(Cluster.class);
		assertThat(cluster.getClusterName(), is(equalTo("Test Cluster")));
	}

	@Test
	public void createClusterWithOverrides() {
		this.context = doLoad("spring.data.cassandra.cluster-name=testcluster");
		assertEquals(1, this.context.getBeanNamesForType(Cluster.class).length);
		Cluster cluster = this.context.getBean(Cluster.class);
		assertThat(cluster.getClusterName(), is(equalTo("testcluster")));
	}

	private AnnotationConfigApplicationContext doLoad(String... environment) {
		AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
		EnvironmentTestUtils.addEnvironment(applicationContext, environment);
		applicationContext.register(PropertyPlaceholderAutoConfiguration.class,
				CassandraAutoConfiguration.class);
		applicationContext.refresh();
		return applicationContext;
	}

}