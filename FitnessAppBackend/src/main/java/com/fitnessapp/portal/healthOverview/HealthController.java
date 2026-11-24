package com.fitnessapp.portal.healthOverview;

import com.fitnessapp.portal.healthOverview.HealthResponseDto.ConnectionPoolDto;
import com.fitnessapp.portal.healthOverview.HealthResponseDto.DatabaseHealthDto;
import com.fitnessapp.portal.healthOverview.HealthResponseDto.JvmHealthDto;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.time.Instant;
import java.util.Map;

import static com.fitnessapp.utils.portalUtil.PortalUtil.PORTAL_OVERVIEW_ENDPOINT;

@RestController
@RequestMapping(PORTAL_OVERVIEW_ENDPOINT)
@RequiredArgsConstructor
public class HealthController {
    private final JdbcTemplate jdbcTemplate;
    private final DataSource dataSource;
    private final ApiLatencyMetrics apiLatencyMetrics;
    private final ConfigurableApplicationContext applicationContext;

    /** Liveness probe: confirms that the Spring application is responding. */
    @GetMapping("/live")
    public Map<String, Object> liveness() {
        return Map.of("status", "UP", "checkedAt", Instant.now());
    }

    /** Readiness probe: checks the database before reporting the app ready for traffic. */
    @GetMapping("/ready")
    public ResponseEntity<Map<String, Object>> readiness() {
        long startedAt = System.nanoTime();
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            long latencyMs = elapsedMs(startedAt);
            return ResponseEntity.ok(Map.of("status", "UP", "database", "UP", "databaseLatencyMs", latencyMs,
                    "checkedAt", Instant.now()));
        } catch (Exception exception) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
                    "status", "DOWN", "database", "DOWN", "checkedAt", Instant.now()));
        }
    }

    /** Detailed diagnostics for the portal, including JVM and Hikari pool information when available. */
    @GetMapping
    public ResponseEntity<HealthResponseDto> health() {
        long startedAt = System.nanoTime();
        DatabaseHealthDto database;
        try {
            jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            database = new DatabaseHealthDto("UP", elapsedMs(startedAt), "PostgreSQL", poolDetails());
        } catch (Exception exception) {
            database = new DatabaseHealthDto("DOWN", elapsedMs(startedAt), "PostgreSQL", poolDetails());
        }

        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        HealthResponseDto response = new HealthResponseDto(
                "UP".equals(database.status()) ? "UP" : "DEGRADED",
                Instant.now(),
                Math.max(0, System.currentTimeMillis() - applicationContext.getStartupDate()),
                database,
                new JvmHealthDto(usedMemory, runtime.maxMemory(), runtime.availableProcessors())
        );
        return ResponseEntity.status("UP".equals(response.status()) ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @GetMapping("/api-latency")
    public ApiLatencyDto apiLatency() {
        return new ApiLatencyDto(Instant.now(), apiLatencyMetrics.requestCount(),
                apiLatencyMetrics.averageLatencyMs(), apiLatencyMetrics.maximumLatencyMs());
    }

    private ConnectionPoolDto poolDetails() {
        if (!(dataSource instanceof HikariDataSource hikari)) {
            return null;
        }
        HikariPoolMXBean pool = hikari.getHikariPoolMXBean();
        return pool == null ? null : new ConnectionPoolDto(hikari.getPoolName(), pool.getActiveConnections(),
                pool.getIdleConnections(), pool.getTotalConnections(), hikari.getMaximumPoolSize(),
                hikari.getMinimumIdle());
    }

    private long elapsedMs(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000L;
    }
}
