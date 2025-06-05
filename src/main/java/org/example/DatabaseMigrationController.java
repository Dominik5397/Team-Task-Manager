package org.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kontroler do monitorowania stanu bazy danych i migracji.
 * Dostępny tylko w profilu development dla bezpieczeństwa.
 */
@RestController
@RequestMapping("/api/database")
@Profile("dev")
public class DatabaseMigrationController {

    @Autowired
    private DataSource dataSource;

    /**
     * Pobiera informacje o stanie bazy danych
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        try {
            Map<String, Object> response = new HashMap<>();
            
            try (Connection conn = dataSource.getConnection()) {
                response.put("timestamp", LocalDateTime.now());
                response.put("databaseUrl", conn.getMetaData().getURL());
                response.put("databaseProduct", conn.getMetaData().getDatabaseProductName());
                response.put("databaseVersion", conn.getMetaData().getDatabaseProductVersion());
                response.put("driverName", conn.getMetaData().getDriverName());
                response.put("driverVersion", conn.getMetaData().getDriverVersion());
                
                // Sprawdzenie tabel
                List<String> tables = new ArrayList<>();
                try (ResultSet rs = conn.getMetaData().getTables(null, null, "%", new String[]{"TABLE"})) {
                    while (rs.next()) {
                        tables.add(rs.getString("TABLE_NAME"));
                    }
                }
                response.put("tables", tables);
                
                // Sprawdzenie tabeli flyway_schema_history jeśli istnieje
                if (tables.contains("flyway_schema_history") || tables.contains("FLYWAY_SCHEMA_HISTORY")) {
                    List<Map<String, Object>> migrations = new ArrayList<>();
                    try (Statement stmt = conn.createStatement();
                         ResultSet rs = stmt.executeQuery("SELECT * FROM \"flyway_schema_history\" ORDER BY \"installed_rank\"")) {
                        while (rs.next()) {
                            Map<String, Object> migration = new HashMap<>();
                            migration.put("version", rs.getString("version"));
                            migration.put("description", rs.getString("description"));
                            migration.put("type", rs.getString("type"));
                            migration.put("script", rs.getString("script"));
                            migration.put("installed_on", rs.getTimestamp("installed_on"));
                            migration.put("execution_time", rs.getInt("execution_time"));
                            migration.put("success", rs.getBoolean("success"));
                            migrations.add(migration);
                        }
                    }
                    response.put("migrations", migrations);
                    response.put("migrationCount", migrations.size());
                } else {
                    response.put("migrations", List.of());
                    response.put("migrationCount", 0);
                    response.put("flywayStatus", "Not initialized");
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "error", "Failed to retrieve database info",
                            "message", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }

    /**
     * Informacje o konfiguracji środowiska
     */
    @GetMapping("/environment")
    public ResponseEntity<Map<String, Object>> getEnvironmentInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("profile", "development");
        response.put("flywayEnabled", "true");
        response.put("ddlAuto", "none (controlled by Flyway)");
        response.put("migrationLocation", "classpath:db/migration");
        response.put("description", "Database migration management with Flyway");
        response.put("features", List.of(
            "Automatic schema migration",
            "Version control for database changes",
            "Safe production deployments",
            "Migration rollback support"
        ));

        return ResponseEntity.ok(response);
    }

    /**
     * Sprawdza dostępność bazy danych
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> checkDatabaseHealth() {
        try {
            try (Connection conn = dataSource.getConnection()) {
                boolean isValid = conn.isValid(5); // 5 sekund timeout
                
                Map<String, Object> response = new HashMap<>();
                response.put("timestamp", LocalDateTime.now());
                response.put("healthy", isValid);
                response.put("status", isValid ? "UP" : "DOWN");
                response.put("message", isValid ? "Database connection is healthy" : "Database connection failed");
                
                return ResponseEntity.ok(response);
            }
        } catch (Exception e) {
            return ResponseEntity.status(503)
                    .body(Map.of(
                            "healthy", false,
                            "status", "DOWN",
                            "error", "Database connection failed",
                            "message", e.getMessage(),
                            "timestamp", LocalDateTime.now()
                    ));
        }
    }
} 