# 🛡️ Przewodnik Walidacji Danych - Team Task Manager

## 📋 Przegląd

Projekt Team Task Manager został rozszerzony o kompletny system walidacji danych wejściowych przy użyciu Jakarta Validation (Bean Validation) oraz **bezpiecznych typów enum** dla statusu i priorytetu zadań. System automatycznie waliduje dane przed zapisem do bazy danych i zwraca użyteczne komunikaty o błędach.

## 🔧 Konfiguracja

### Zależności
```kotlin
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-validation")
}
```

## 📝 Walidacja Encji

### Encja User
```java
@Entity
public class User {
    @NotBlank(message = "Username is required")
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    private String username;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;
    
    @Size(max = 500, message = "Avatar URL must not exceed 500 characters")
    @Pattern(regexp = "^(https?://.*\\.(jpg|jpeg|png|gif|webp))$|^$", 
             message = "Avatar URL must be a valid image URL")
    private String avatarUrl;
}
```

#### Reguły walidacji User:
- **username**: Wymagane, 2-50 znaków
- **email**: Wymagane, poprawny format email, max 100 znaków
- **avatarUrl**: Opcjonalne, musi być poprawnym URL obrazu (jpg, jpeg, png, gif, webp)

### 🎯 Encja Task z Enumami
```java
@Entity
public class Task {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 100, message = "Title must be between 3 and 100 characters")
    private String title;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @Future(message = "Due date must be in the future")
    private LocalDate dueDate;
    
    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @JsonSerialize(using = TaskStatusConverter.Serializer.class)
    @JsonDeserialize(using = TaskStatusConverter.Deserializer.class)
    private TaskStatus status;
    
    @NotNull(message = "Priority is required")
    @Enumerated(EnumType.STRING)
    @JsonSerialize(using = TaskPriorityConverter.Serializer.class)
    @JsonDeserialize(using = TaskPriorityConverter.Deserializer.class)
    private TaskPriority priority;
}
```

#### Reguły walidacji Task:
- **title**: Wymagane, 3-100 znaków
- **description**: Opcjonalne, max 1000 znaków
- **dueDate**: Opcjonalne, musi być w przyszłości
- **status**: Wymagane, enum TaskStatus (TODO, IN_PROGRESS, DONE)
- **priority**: Wymagane, enum TaskPriority (LOW, MEDIUM, HIGH)

## 🎨 Enumy dla Status i Priorytet

### TaskStatus Enum
```java
public enum TaskStatus {
    TODO("To Do"),
    IN_PROGRESS("In Progress"), 
    DONE("Done");
    
    // Metody pomocnicze dostępne w encji Task:
    task.isCompleted()    // status == DONE
    task.isInProgress()   // status == IN_PROGRESS
}
```

**Obsługiwane formaty wejściowe:**
- `"To Do"`, `"TODO"`, `"TO_DO"`
- `"In Progress"`, `"IN_PROGRESS"`, `"INPROGRESS"`
- `"Done"`, `"DONE"`

### TaskPriority Enum
```java
public enum TaskPriority {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3);
    
    // Metody pomocnicze dostępne w encji Task:
    task.isHighPriority()           // priority == HIGH
    priority.isHigherThan(other)    // porównanie priorytetów
    priority.isLowerThan(other)     // porównanie priorytetów
}
```

**Obsługiwane formaty wejściowe:**
- `"Low"`, `"LOW"`
- `"Medium"`, `"MEDIUM"`
- `"High"`, `"HIGH"`

### 🔄 Korzyści z Enumów
1. **Bezpieczeństwo typów** - kompilator weryfikuje poprawność wartości
2. **Spójność danych** - brak problemów z różnymi formatami ("To Do" vs "todo")
3. **Intellisense** - automatyczne uzupełnianie w IDE
4. **Refactoring** - łatwe zmiany nazw w całym projekcie
5. **Logika biznesowa** - metody pomocnicze (porównania, sprawdzenia)

## 🎯 Kontrolery z Walidacją

### UserController
```java
@PostMapping
public User createUser(@Valid @RequestBody User user) {
    return userRepository.save(user);
}

@PutMapping("/{id}")
public User updateUser(@PathVariable Long id, @Valid @RequestBody User user) {
    user.setId(id);
    return userRepository.save(user);
}
```

### TaskController
```java
@PostMapping
public Task createTask(@Valid @RequestBody Task task) {
    // logika tworzenia zadania
    return taskRepository.save(task);
}

@PutMapping("/{id}")
public Task updateTask(@PathVariable Long id, @Valid @RequestBody Task task) {
    // logika aktualizacji zadania
    return taskRepository.save(task);
}
```

## 🚨 Obsługa Błędów

### GlobalExceptionHandler
```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("message", "Invalid input data");
        response.put("errors", errors);
        
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
```

### Przykład odpowiedzi błędu walidacji:
```json
{
  "timestamp": "2024-01-15T10:30:00",
  "status": 400,
  "error": "Validation Failed",
  "message": "Invalid input data",
  "errors": {
    "username": "Username is required",
    "email": "Email should be valid"
  }
}
```

### Przykład błędu nieprawidłowego enum:
```json
{
  "error": "Internal Server Error",
  "message": "Invalid task status: Invalid Status. Valid values are: To Do, In Progress, Done",
  "status": 500
}
```

## 🧪 Testowanie Walidacji

### Przykłady testów jednostkowych:
```java
@Test
public void testTaskValidation_ValidEnums() {
    Task task = new Task();
    task.setTitle("Test Task");
    task.setStatus(TaskStatus.TODO);
    task.setPriority(TaskPriority.HIGH);
    
    Set<ConstraintViolation<Task>> violations = validator.validate(task);
    assertThat(violations).isEmpty();
}

@Test
public void testTaskStatus_EnumValues() {
    assertThat(TaskStatus.TODO.getDisplayName()).isEqualTo("To Do");
    assertThat(TaskPriority.HIGH.isHigherThan(TaskPriority.LOW)).isTrue();
}
```

## 🔍 Endpointy API

### Pobieranie wartości enumów
```bash
GET /api/enums/all
```

**Odpowiedź:**
```json
{
  "taskStatuses": [
    {"value": "TODO", "displayName": "To Do", "label": "To Do"},
    {"value": "IN_PROGRESS", "displayName": "In Progress", "label": "In Progress"},
    {"value": "DONE", "displayName": "Done", "label": "Done"}
  ],
  "taskPriorities": [
    {"value": "LOW", "displayName": "Low", "label": "Low", "level": 1},
    {"value": "MEDIUM", "displayName": "Medium", "label": "Medium", "level": 2},
    {"value": "HIGH", "displayName": "High", "label": "High", "level": 3}
  ]
}
```

### Endpoint testowy walidacji użytkowników:
```bash
POST /api/validation-test/test-user
Content-Type: application/json

{
    "username": "",
    "email": "invalid-email",
    "avatarUrl": "not-an-image-url"
}
```

### Endpoint testowy walidacji zadań z enumami:
```bash
POST /api/validation-test/test-task
Content-Type: application/json

{
    "title": "Test Task",
    "status": "To Do",
    "priority": "High"
}
```

## 📊 Przykłady Użycia

### Tworzenie prawidłowego użytkownika:
```bash
POST /api/users
Content-Type: application/json

{
    "username": "Jan Kowalski",
    "email": "jan.kowalski@example.com",
    "avatarUrl": "https://example.com/avatar.jpg"
}
```

### Tworzenie prawidłowego zadania z enumami:
```bash
POST /api/tasks
Content-Type: application/json

{
    "title": "Implementacja enumów",
    "description": "Dodanie bezpiecznych typów enum",
    "status": "To Do",
    "priority": "High",
    "dueDate": "2024-12-31"
}
```

### Alternatywne formaty enum (wszystkie poprawne):
```bash
# Różne formaty statusu
"status": "To Do"           # displayName
"status": "TODO"            # enum name
"status": "IN_PROGRESS"     # enum name with underscore

# Różne formaty priorytetu  
"priority": "High"          # displayName
"priority": "HIGH"          # enum name
"priority": "MEDIUM"        # enum name
```

## 📈 Korzyści z Nowego Systemu

1. **Bezpieczeństwo typów**: Enumy eliminują błędy związane z różnymi formatami stringów
2. **Jakość danych**: Gwarantowana spójność i poprawność danych
3. **User Experience**: Jasne komunikaty o błędach dla użytkowników
4. **Czytelność kodu**: Deklaratywne reguły walidacji + bezpieczne typy
5. **Automatyzacja**: Automatyczna walidacja na poziomie frameworka
6. **Elastyczność**: Obsługa różnych formatów wejściowych
7. **Logika biznesowa**: Metody pomocnicze w enumach

## 🎯 Najlepsze Praktyki

1. **Używaj enumów zamiast stringów** dla wartości z ograniczonym zbiorem
2. **Definiuj jasne komunikaty błędów** w języku użytkownika
3. **Testuj wszystkie scenariusze walidacji** w testach jednostkowych
4. **Obsługuj błędy globalnie** za pomocą @ControllerAdvice
5. **Dokumentuj reguły walidacji** dla zespołu i użytkowników API
6. **Używaj konwerterów JSON** dla kompatybilności z frontendem
7. **Dodawaj metody pomocnicze** do enumów dla logiki biznesowej

## 🚀 Rozszerzenia

Możliwe rozszerzenia systemu walidacji:
- Walidacja krzyżowa pól (custom validators)
- Walidacja grupowa (@Validated z grupami)
- Walidacja asynchroniczna
- Integracja z systemem logowania błędów
- Lokalizacja komunikatów błędów
- Dodatkowe enumy (TaskCategory, UserRole, etc.)
- Walidacja na poziomie bazy danych (constraints) 