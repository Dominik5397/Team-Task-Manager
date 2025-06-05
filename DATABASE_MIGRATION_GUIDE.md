# Database Migration Management Guide

## Przegląd

Ten dokument opisuje implementację profesjonalnego zarządzania migracjami bazy danych w projekcie Team Task Manager przy użyciu **Flyway** z konfiguracją opartą na profilach Spring Boot.

## Problem

Pierwotna konfiguracja używała `spring.jpa.hibernate.ddl-auto=update`, co jest **niebezpieczne w środowiskach produkcyjnych** ponieważ:

- Hibernate może modyfikować schemat w sposób niekontrolowany
- Brak kontroli wersji zmian w bazie danych  
- Ryzyko utraty danych przy nieprzewidzianych zmianach
- Brak możliwości rollback'u
- Brak audytu zmian w schemacie

## Rozwiązanie

Implementacja **Flyway** z konfiguracją opartą na profilach:

### 1. Zależności

```kotlin
// build.gradle.kts
dependencies {
    // Database migration with Flyway
    implementation("org.flywaydb:flyway-core")
    // ... inne zależności
}
```

### 2. Konfiguracja Profilów

#### Profil Bazowy (`application.properties`)
```properties
# Default profile configuration
spring.profiles.active=dev

# Common H2 database configuration
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.h2.console.enabled=true

# Flyway configuration
spring.flyway.enabled=false
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=0

# Disable Hibernate DDL generation (Flyway will handle schema)
spring.jpa.hibernate.ddl-auto=update

# JPA/Hibernate common settings
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.highlight_sql=true
```

#### Profil Development (`application-dev.properties`)
```properties
# Development Profile Configuration

# Development database settings
spring.datasource.url=jdbc:h2:mem:devdb
spring.h2.console.enabled=true

# Development Flyway settings
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=true
spring.flyway.clean-disabled=false

# Development JPA settings
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.hibernate.ddl-auto=none

# Logging configuration for development
logging.level.org.flywaydb=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE
logging.level.org.springframework.transaction=DEBUG

# Development specific settings
spring.devtools.restart.enabled=true
management.endpoints.web.exposure.include=health,info,flyway
```

#### Profil Production (`application-prod.properties`)
```properties
# Production Profile Configuration

# Production database settings (example with PostgreSQL)
# spring.datasource.url=jdbc:postgresql://localhost:5432/taskmanager_prod
# spring.datasource.username=${DB_USERNAME}
# spring.datasource.password=${DB_PASSWORD}
# spring.datasource.driver-class-name=org.postgresql.Driver
# spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect

# For demo purposes, using H2 with file storage
spring.datasource.url=jdbc:h2:file:./data/taskmanager_prod;AUTO_SERVER=TRUE
spring.h2.console.enabled=false

# Production Flyway settings - STRICT
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=false
spring.flyway.clean-disabled=true
spring.flyway.validate-on-migrate=true
spring.flyway.out-of-order=false

# Production JPA settings - STRICT
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Production logging - minimal
logging.level.org.flywaydb=INFO
logging.level.org.hibernate=WARN
logging.level.org.springframework=WARN

# Security settings
management.endpoints.web.exposure.include=health
server.error.include-stacktrace=never
server.error.include-message=never
```

#### Profil Test (`application-test.properties`)
```properties
# Test Profile Configuration

# Test database settings - in-memory for speed
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.h2.console.enabled=false

# Temporarily disable Flyway in tests - use Hibernate DDL for testing
spring.flyway.enabled=false

# Test JPA settings - use create-drop for fast test setup
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false

# Faster test execution
spring.jpa.properties.hibernate.jdbc.batch_size=50
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Test logging - minimal
logging.level.org.flywaydb=INFO
logging.level.org.hibernate=WARN
logging.level.org.springframework=WARN

# Test specific settings
spring.test.database.replace=none
```

### 3. Migracje Flyway

#### Struktura Folderów
```
src/main/resources/db/migration/
├── V1__Create_user_table.sql
├── V2__Create_task_table.sql
└── V3__Insert_sample_data.sql
```

#### V1__Create_user_table.sql
```sql
-- Migration V1: Create user table
CREATE TABLE app_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    avatar_url VARCHAR(500)
);

CREATE INDEX idx_user_email ON app_user(email);
CREATE INDEX idx_user_username ON app_user(username);
```

#### V2__Create_task_table.sql
```sql
-- Migration V2: Create task table
CREATE TABLE task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description VARCHAR(1000),
    due_date DATE,
    status VARCHAR(20) NOT NULL,
    priority VARCHAR(10) NOT NULL,
    user_id BIGINT,
    change_log TEXT,
    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE SET NULL
);

CREATE INDEX idx_task_status ON task(status);
CREATE INDEX idx_task_priority ON task(priority);
CREATE INDEX idx_task_user_id ON task(user_id);
CREATE INDEX idx_task_due_date ON task(due_date);
```

#### V3__Insert_sample_data.sql
```sql
-- Migration V3: Insert sample data
INSERT INTO app_user (username, email, avatar_url) VALUES 
    ('Jan Kowalski', 'jan.kowalski@example.com', 'https://randomuser.me/api/portraits/men/1.jpg'),
    ('Anna Nowak', 'anna.nowak@example.com', 'https://randomuser.me/api/portraits/women/2.jpg'),
    ('Piotr Wiśniewski', 'piotr.wisniewski@example.com', 'https://randomuser.me/api/portraits/men/3.jpg');

INSERT INTO task (title, description, due_date, status, priority, user_id, change_log) VALUES 
    ('Implementacja logowania', 'Dodanie systemu logowania użytkowników', '2025-06-15', 'TODO', 'HIGH', 1, '[]'),
    ('Optymalizacja bazy danych', 'Analiza i optymalizacja zapytań SQL', '2025-06-20', 'IN_PROGRESS', 'MEDIUM', 2, '[]'),
    ('Testy jednostkowe', 'Napisanie testów dla API', '2025-06-10', 'DONE', 'HIGH', 3, '[]'),
    ('Dokumentacja API', 'Utworzenie dokumentacji OpenAPI', '2025-06-25', 'TODO', 'LOW', 1, '[]');
```

### 4. Kontroler Monitorowania (tylko dev)

```java
@RestController
@RequestMapping("/api/database")
@Profile("dev")
public class DatabaseMigrationController {
    
    @GetMapping("/environment")
    public ResponseEntity<Map<String, Object>> getDatabaseEnvironment() {
        // Informacje o środowisku
    }
    
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getDatabaseInfo() {
        // Informacje o migracjach i tabelach
    }
}
```

## Uruchamianie z Różnymi Profilami

### Development (domyślny)
```bash
./gradlew bootRun
# lub
SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun
```

### Production
```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

### Test
```bash
./gradlew test
```

## Testowanie API

### Sprawdzenie środowiska (tylko dev)
```bash
curl http://localhost:8080/api/database/environment
```

### Informacje o migracjach (tylko dev)
```bash
curl http://localhost:8080/api/database/info
```

### Testowanie danych
```bash
curl http://localhost:8080/api/users
curl http://localhost:8080/api/tasks
```

## Korzyści Implementacji

### 🔒 Bezpieczeństwo
- **Produkcja**: `ddl-auto=validate` - tylko weryfikacja schematu
- **Development**: `ddl-auto=none` - kontrola przez Flyway
- **Test**: `ddl-auto=create-drop` - szybkie testy

### 📊 Kontrola Wersji
- Wszystkie zmiany w bazie danych są wersjonowane
- Historia migracji w tabeli `flyway_schema_history`
- Możliwość rollback'u i audytu

### 🚀 Deployment
- Automatyczne migracje przy starcie aplikacji
- Walidacja integralności schematu
- Bezpieczne wdrożenia produkcyjne

### 🔧 Development
- Łatwe zarządzanie zmianami w schemacie
- Konsystentność między środowiskami
- Debugging i monitoring migracji

## Najlepsze Praktyki

### 1. Nazewnictwo Migracji
```
V{version}__{description}.sql
V1__Create_user_table.sql
V2__Add_user_avatar_column.sql
V3__Create_task_table.sql
```

### 2. Nieodwracalne Zmiany
- Nigdy nie modyfikuj istniejących migracji
- Zawsze twórz nowe migracje dla zmian
- Używaj `ALTER TABLE` zamiast `DROP/CREATE`

### 3. Testowanie Migracji
- Testuj migracje na kopii danych produkcyjnych
- Sprawdzaj wydajność na dużych tabelach
- Przygotuj plan rollback'u

### 4. Środowiska
- **Development**: Flyway enabled, verbose logging
- **Production**: Flyway enabled, strict validation, minimal logging
- **Test**: Hibernate DDL dla szybkości testów

## Rozwiązywanie Problemów

### Problem: Migracja nie działa
```bash
# Sprawdź status migracji
curl http://localhost:8080/api/database/info

# Sprawdź logi Flyway
logging.level.org.flywaydb=DEBUG
```

### Problem: Konflikt schematu
```bash
# W development można wyczyścić bazę
spring.flyway.clean-disabled=false
```

### Problem: Testy nie działają
```bash
# Upewnij się, że testy używają create-drop
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
```

## Migracja z Istniejącego Projektu

1. **Backup bazy danych**
2. **Dodaj zależność Flyway**
3. **Utwórz migracje dla istniejącego schematu**
4. **Ustaw `baseline-on-migrate=true`**
5. **Zmień `ddl-auto` na `none`**
6. **Przetestuj na środowisku dev**

## Podsumowanie

Implementacja Flyway z profilami Spring Boot zapewnia:

- ✅ **Bezpieczne zarządzanie schematem bazy danych**
- ✅ **Kontrolę wersji zmian w bazie danych**
- ✅ **Różne konfiguracje dla różnych środowisk**
- ✅ **Automatyczne migracje przy deployment**
- ✅ **Audyt i możliwość rollback'u**
- ✅ **Profesjonalne podejście do DevOps**

Ta implementacja eliminuje ryzyko związane z `ddl-auto=update` w produkcji i zapewnia kontrolowane, bezpieczne zarządzanie schematem bazy danych. 