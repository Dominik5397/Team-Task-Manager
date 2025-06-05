# ObjectMapper Refactoring - Centralne zarządzanie JSON

## Przegląd zmian

Przeprowadzono refaktoryzację zarządzania `ObjectMapper` w aplikacji Team Task Manager, przechodząc od lokalnych instancji do centralnej konfiguracji Spring Bean.

## Problem przed refaktoryzacją

### Problematyczne podejście:
```java
// W TaskServiceImpl - lokalna instancja
private final ObjectMapper objectMapper = new ObjectMapper();
```

#### Problemy:
- **Brak spójności**: Różne komponenty mogły mieć różne konfiguracje JSON
- **Duplikacja konfiguracji**: Konieczność powtarzania ustawień w wielu miejscach
- **Trudne zarządzanie**: Brak centralnego miejsca do zmiany konfiguracji JSON
- **Testowanie**: Trudność w testowaniu z różnymi konfiguracjami

## Rozwiązanie - Centralna konfiguracja

### 1. Utworzenie JsonConfig

**Plik:** `src/main/java/org/example/config/JsonConfig.java`

```java
@Configuration
public class JsonConfig {

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Rejestracja modułu Java Time dla obsługi LocalDate, LocalDateTime
        mapper.registerModule(new JavaTimeModule());
        
        // Wyłączenie zapisywania dat jako timestamps - używamy ISO strings
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        
        // Ignorowanie nieznanych pól podczas deserializacji
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        
        // Ignorowanie pustych właściwości (null values) w JSON
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        
        // Czytelne formatowanie JSON (wcięcia) - przydatne do debugowania
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        
        // Obsługa pustych obiektów - nie rzucanie wyjątków
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        
        return mapper;
    }
}
```

### 2. Aktualizacja TaskServiceImpl

**Przed:**
```java
private final ObjectMapper objectMapper = new ObjectMapper();
```

**Po:**
```java
@Autowired
private ObjectMapper objectMapper;
```

### 3. Usprawnienia w change logging

#### Ulepszone dodawanie wpisów do loga:
```java
private void addChangeLogEntry(Task task, String action, String description) {
    try {
        List<Map<String, Object>> log = getCurrentChangeLog(task);
        
        Map<String, Object> logEntry = Map.of(
            "timestamp", LocalDateTime.now(), // Teraz obsługiwane przez JavaTimeModule
            "action", action,
            "description", description,
            "title", task.getTitle() != null ? task.getTitle() : ""
        );
        
        log.add(logEntry);
        task.setChangeLog(objectMapper.writeValueAsString(log));
    } catch (Exception e) {
        // Ulepszona obsługa błędów z informacją o wyjątku
        String errorLog = String.format(
            "[{\"timestamp\":\"%s\",\"action\":\"%s\",\"description\":\"%s\",\"error\":\"JSON serialization failed: %s\"}]",
            LocalDateTime.now().toString(), action, description, e.getMessage()
        );
        task.setChangeLog(errorLog);
    }
}
```

#### Bardziej szczegółowe logowanie aktualizacji:
```java
private void addUpdateChangeLog(Task newTask, Task oldTask) {
    List<String> changes = new ArrayList<>();
    
    // Szczegółowe śledzenie zmian z formatowaniem
    if (!newTask.getTitle().equals(oldTask.getTitle())) {
        changes.add(String.format("title from '%s' to '%s'", 
            oldTask.getTitle(), newTask.getTitle()));
    }
    
    // Skrócone opisy dla długich tekstów
    if (!Objects.equals(newTask.getDescription(), oldTask.getDescription())) {
        String oldDesc = truncateDescription(oldTask.getDescription());
        String newDesc = truncateDescription(newTask.getDescription());
        changes.add(String.format("description from '%s' to '%s'", oldDesc, newDesc));
    }
    
    // Analogicznie dla innych pól...
    
    if (!changes.isEmpty()) {
        String description = "Updated: " + String.join("; ", changes);
        addChangeLogEntry(newTask, "updated", description);
    }
}
```

## Konfiguracja ObjectMapper

### Funkcjonalności:

#### 1. **Java Time Module**
- Obsługa `LocalDate`, `LocalDateTime`, `LocalTime`
- Automatyczna serializacja do ISO 8601 strings
- Deserializacja z różnych formatów dat

#### 2. **Formatowanie dat**
```json
{
  "createdAt": "2025-06-05T12:30:45.123456789",
  "dueDate": "2025-06-10"
}
```
Zamiast timestamps:
```json
{
  "createdAt": 1733395845123,
  "dueDate": 1733395845000
}
```

#### 3. **Ignorowanie null wartości**
```java
// Wejście
Map<String, Object> data = Map.of(
    "title", "Task Title",
    "description", null,
    "priority", "HIGH"
);

// Wyjście JSON
{
  "title": "Task Title",
  "priority": "HIGH"
  // description jest pominięte
}
```

#### 4. **Ignorowanie nieznanych pól**
```json
// JSON wejściowy z nieznananymi polami
{
  "title": "Task Title",
  "unknownField": "ignored",
  "anotherUnknown": 123
}

// Deserializacja się powiedzie, nieznane pola zostaną zignorowane
```

#### 5. **Czytelne formatowanie**
```json
{
  "id" : 1,
  "title" : "Task Title",
  "description" : "Task Description",
  "status" : "To Do"
}
```

## Testowanie

### 1. Testy jednostkowe - JsonConfigTest

**Plik:** `src/test/java/org/example/JsonConfigTest.java`

```java
@SpringBootTest
public class JsonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void objectMapper_ShouldSerializeDatesAsISOStrings() {
        // Test formatowania dat
    }

    @Test
    void objectMapper_ShouldIgnoreNullValues() {
        // Test ignorowania null
    }

    @Test
    void objectMapper_ShouldIgnoreUnknownPropertiesOnDeserialization() {
        // Test ignorowania nieznanych pól
    }
}
```

### 2. Kontroler testowy - JsonTestController

**Endpointy do testowania:**

```
GET  /api/json-test/date-formatting      - Test formatowania dat
GET  /api/json-test/null-handling         - Test ignorowania null
GET  /api/json-test/formatted-json        - Test formatowania JSON
POST /api/json-test/unknown-fields        - Test nieznanych pól
GET  /api/json-test/object-mapper-demo    - Demo ObjectMapper
POST /api/json-test/validate-json-config  - Walidacja konfiguracji
```

#### Przykład użycia:
```bash
curl -s "http://localhost:8080/api/json-test/date-formatting"
```

**Odpowiedź:**
```json
{
  "futureDate" : "2025-07-05",
  "currentDateTime" : "2025-06-05T12:20:08.065797057",
  "pastDateTime" : "2025-06-05T07:20:08.065839686",
  "currentDate" : "2025-06-05",
  "description" : "Test formatowania dat - null wartości są ignorowane"
}
```

## Korzyści refaktoryzacji

### 1. **Centralne zarządzanie**
- Jedna konfiguracja dla całej aplikacji
- Łatwe wprowadzanie globalnych zmian
- Spójność w całym systemie

### 2. **Maintainability**
- Prostsze zarządzanie konfiguracją JSON
- Centralne miejsce dla wszystkich ustawień serializacji
- Łatwiejsze debugowanie problemów z JSON

### 3. **Performance**
- Jedna instancja ObjectMapper dla całej aplikacji
- Brak duplikacji instancji
- Optymalizacja pamięci

### 4. **Testowalność**
- Łatwe testowanie z wstrzykniętym ObjectMapper
- Spójne zachowanie w testach i produkcji
- Mockowanie w testach jednostkowych

### 5. **Elastyczność**
- Łatwe dodawanie nowych modułów Jackson
- Prosta konfiguracja nowych funkcjonalności
- Możliwość tworzenia profili konfiguracji dla różnych środowisk

## Przykłady użycia

### 1. Change Logging z nową konfiguracją

```java
// Tworzenie zadania
Task task = new Task();
task.setTitle("New Task");
task.setStatus(TaskStatus.TODO);

Task created = taskService.createTask(task);

// Automatycznie dodany changeLog z poprawnym formatowaniem dat
```

**Wynik w changeLog:**
```json
[{
  "timestamp": "2025-06-05T12:30:45.123456789",
  "action": "created",
  "description": "Task created: New Task",
  "title": "New Task"
}]
```

### 2. Serializacja Task z enum'ami

```java
Task task = new Task();
task.setStatus(TaskStatus.IN_PROGRESS);
task.setPriority(TaskPriority.HIGH);
task.setDueDate(LocalDate.now().plusDays(7));

// Automatyczna serializacja z poprawnym formatowaniem
```

**Wynik JSON:**
```json
{
  "status": "In Progress",
  "priority": "High",
  "dueDate": "2025-06-12",
  "completed": false,
  "highPriority": true,
  "inProgress": true
}
```

## Migracja istniejącego kodu

### 1. Usuń lokalne instancje
```java
// PRZED - usuń to
private final ObjectMapper objectMapper = new ObjectMapper();

// PO - dodaj to
@Autowired
private ObjectMapper objectMapper;
```

### 2. Wykorzystaj nowe funkcjonalności
```java
// Korzystaj z automatycznego formatowania dat
Map<String, Object> logEntry = Map.of(
    "timestamp", LocalDateTime.now(), // Automatycznie sformatowane
    "action", action,
    "description", description
);
```

### 3. Testuj konfigurację
```java
@Test
void shouldUseConfiguredObjectMapper() {
    // Test wykorzystujący wstrzykniętą instancję
    String json = objectMapper.writeValueAsString(testObject);
    assertThat(json).contains("2025-06-05"); // ISO format daty
}
```

## Podsumowanie

Refaktoryzacja ObjectMapper przyniosła następujące usprawnienia:

✅ **Centralne zarządzanie konfiguracją JSON**  
✅ **Spójne formatowanie dat jako ISO strings**  
✅ **Automatyczne ignorowanie null wartości**  
✅ **Czytelne formatowanie JSON z wcięciami**  
✅ **Obsługa nieznanych pól bez błędów**  
✅ **Lepsze logowanie zmian z formatowaniem dat**  
✅ **Kompleksowe testy konfiguracji**  
✅ **Endpoint'y do testowania funkcjonalności**  

Aplikacja zyskała lepszą architekturę, łatwiejsze zarządzanie konfiguracją JSON i bardziej spójne zachowanie w całym systemie. 