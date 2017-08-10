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

package org.springframework.boot.actuate.metrics.scheduling;

import java.util.concurrent.CountDownLatch;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.EnableMetrics;
import org.springframework.boot.actuate.metrics.SpringBootTestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class MetricsSchedulingAspectTest {

	static CountDownLatch longTaskStarted = new CountDownLatch(1);
	static CountDownLatch longTaskShouldComplete = new CountDownLatch(1);

	static CountDownLatch shortBeepsExecuted = new CountDownLatch(2);

	@Autowired
	private MeterRegistry registry;

	@Autowired
	private ThreadPoolTaskScheduler scheduler;

	@Test
	public void scheduledIsInstrumented() throws InterruptedException {
		shortBeepsExecuted.await();
		while (this.scheduler.getActiveCount() > 1) {
		}

		Assertions.assertThat(this.registry.findMeter(Timer.class, "beeper"))
				.hasValueSatisfying(t -> assertThat(t.count()).isEqualTo(2));

		Assertions.assertThat(
				this.registry.findMeter(Gauge.class, "beeper.quantiles", "quantile", "0.5"))
				.isNotEmpty();
		assertThat(
				this.registry.findMeter(Gauge.class, "beeper.quantiles", "quantile", "0.95"))
				.isNotEmpty();

		longTaskStarted.await();

		Assertions.assertThat(this.registry.findMeter(LongTaskTimer.class, "longBeep"))
				.hasValueSatisfying(t -> assertThat(t.activeTasks()).isEqualTo(1));

		// make sure longBeep continues running until we have a chance to observe it in
		// the active state
		longTaskShouldComplete.countDown();

		while (this.scheduler.getActiveCount() > 0) {
		}

		// now the long beeper has contributed to the beep count as well
		assertThat(this.registry.findMeter(Timer.class, "beeper")).hasValueSatisfying(
				t -> assertThat(t.count()).isEqualTo(3));
	}

	@SpringBootTestApplication
	@EnableMetrics
	@EnableScheduling
	static class MetricsApp {
		@Bean
		MeterRegistry registry() {
			return new SimpleMeterRegistry();
		}

		@Bean
		ThreadPoolTaskScheduler scheduler() {
			ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
			// this way, executing longBeep doesn't block the short tasks from running
			scheduler.setPoolSize(6);
			return scheduler;
		}

		@Timed("beeper")
		@Timed(value = "longBeep", longTask = true)
		@Scheduled(fixedRate = 1000)
		void longBeep() throws InterruptedException {
			longTaskStarted.countDown();
			longTaskShouldComplete.await();
			System.out.println("beep");
		}

		@Timed("beeper")
		@Scheduled(fixedRate = 1000)
		void beep1() {
			shortBeepsExecuted.countDown();
			System.out.println("beep");
		}

		@Timed(value = "beeper", quantiles = { 0.5, 0.95 })
		@Scheduled(fixedRate = 1000)
		void beep2() {
			shortBeepsExecuted.countDown();
			System.out.println("beep");
		}

		@Timed
		// not instrumented because @Timed lacks a metric name
		@Scheduled(fixedRate = 1000)
		void beep3() {
			System.out.println("beep");
		}

		@Scheduled(fixedRate = 1000)
		// not instrumented because it isn't @Timed
		void beep4() {
			System.out.println("beep");
		}
	}
}
