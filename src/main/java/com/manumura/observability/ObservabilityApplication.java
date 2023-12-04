package com.manumura.observability;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@SpringBootApplication
public class ObservabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(ObservabilityApplication.class, args);
	}

}

@Configuration
@EnableAspectJAutoProxy
class AutoTimingConfiguration {
	@Bean
	public TimedAspect timedAspect(MeterRegistry registry) {
		return new TimedAspect(registry);
	}
}

@RestController
class ReadController {
	private final MeterRegistry meterRegistry;
	private final Random random = new Random();

	public ReadController(MeterRegistry meterRegistry) {
		this.meterRegistry = meterRegistry;
	}

	@GetMapping("/read")
	@Timed(histogram = true, percentiles = {0.5, 0.75, 0.95, 0.99, 0.9999})
	public String read() {
		Counter counter = Counter.builder("api_read_get")
				.description("a number of requests to /read endpoint")
				.register(meterRegistry);
		counter.increment();

		try {
			Thread.sleep(10 + random.nextLong(50));
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new RuntimeException(e);
		}
		return "Done";
	}
}
