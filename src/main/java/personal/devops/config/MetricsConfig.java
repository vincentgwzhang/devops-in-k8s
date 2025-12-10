package personal.devops.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Random;

@Configuration
public class MetricsConfig {

    private final Random random = new Random();

    @Bean
    public CommandLineRunner debugMeters(MeterRegistry registry) {
        return args -> {
            System.out.println("==== Registered Meters ====");
            registry.getMeters().forEach(m ->
                    System.out.println(m.getId().getName() + " | " + m.getId().getType())
            );
        };
    }

    // Counter
    @Bean
    public CommandLineRunner counterRunner(MeterRegistry registry) {
        return args -> {
            new Thread(() -> {
                Counter orderCounter = Counter.builder("app_orders_created").description("orders created").register(registry);

                while (true) {
                    orderCounter.increment();
                    try { Thread.sleep(1000 + random.nextInt(4000)); } catch (Exception ignored) {}
                }
            }).start();
        };
    }

    // CPU Gauge
    @Bean
    public CommandLineRunner cpuGaugeRunner(MeterRegistry registry) {
        return args -> new Thread(() -> {
            class CpuHolder { double value = 0; }
            CpuHolder cpu = new CpuHolder();

            Gauge.builder("app_cpu_usage_percent", cpu, c -> c.value)
                    .description("Simulated CPU usage percent")
                    .register(registry);

            while (true) {
                cpu.value = random.nextDouble() * 100;
                try { Thread.sleep(1000); } catch (Exception ignored) {}
            }
        }).start();
    }

    // Memory Gauge
    @Bean
    public CommandLineRunner memoryGaugeRunner(MeterRegistry registry) {
        return args -> new Thread(() -> {
            class MemoryHolder { double used = 0; }
            MemoryHolder mem = new MemoryHolder();

            Gauge.builder("app_memory_used_mb", mem, m -> m.used)
                    .description("Simulated Memory usage MB")
                    .register(registry);

            while (true) {
                mem.used += random.nextDouble() * 10;
                try { Thread.sleep(2000); } catch (Exception ignored) {}
            }
        }).start();
    }

    // Histogram
    @Bean
    public CommandLineRunner histogramRunner(MeterRegistry registry) {
        return args -> new Thread(() -> {
            DistributionSummary histogram = DistributionSummary.builder("app_response_time_ms")
                    .description("Response time distribution")
                    .baseUnit("milliseconds")
                    .serviceLevelObjectives(50.0, 100.0, 200.0, 300.0, 500.0)
                    .publishPercentileHistogram()
                    .register(registry);

            while (true) {
                double simulated = 5 + random.nextDouble() * 495;
                histogram.record(simulated);
                try { Thread.sleep(500); } catch (Exception ignored) {}
            }
        }).start();
    }

    // Summary (Timer)
    @Bean
    public CommandLineRunner summaryRunner(MeterRegistry registry) {
        return args -> new Thread(() -> {
            Timer timer = Timer.builder("app_request_processing_time")
                    .publishPercentiles(0.5, 0.9, 0.95, 0.99)
                    .publishPercentileHistogram()
                    .register(registry);

            while (true) {
                long start = System.nanoTime();
                try { Thread.sleep(10 + random.nextInt(90)); } catch (Exception ignored) {}
                timer.record(Duration.ofNanos(System.nanoTime() - start));
            }
        }).start();
    }
}

