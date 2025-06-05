# 🛡️ Przewodnik Walidacji Danych - Team Task Manager

## 📋 Przegląd

Projekt Team Task Manager został rozszerzony o kompletny system walidacji danych wejściowych przy użyciu Jakarta Validation (Bean Validation). System automatycznie waliduje dane przed zapisem do bazy danych i zwraca użyteczne komunikaty o błędach.

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

### Encja Task
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
    
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(To Do|In Progress|Done)$", 
             message = "Status must be one of: To Do, In Progress, Done")
    private String status;
    
    @NotBlank(message = "Priority is required")
    @Pattern(regexp = "^(Low|Medium|High)$", 
             message = "Priority must be one of: Low, Medium, High")
    private String priority;
}
```

#### Reguły walidacji Task:
- **title**: Wymagane, 3-100 znaków
- **description**: Opcjonalne, max 1000 znaków
- **dueDate**: Opcjonalne, musi być w przyszłości
- **status**: Wymagane, jedna z wartości: "To Do", "In Progress", "Done"
- **priority**: Wymagane, jedna z wartości: "Low", "Medium", "High"

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
    "email": "Email should be valid",
    "priority": "Priority must be one of: Low, Medium, High"
  }
}
```

## 🧪 Testowanie Walidacji

### Przykłady testów jednostkowych:
```java
@Test
public void testUserValidation_InvalidEmail() {
    User user = new User();
    user.setUsername("Jan Kowalski");
    user.setEmail("invalid-email");
    
    Set<ConstraintViolation<User>> violations = validator.validate(user);
    assertThat(violations).hasSize(1);
    assertThat(violations.iterator().next().getMessage())
        .contains("Email should be valid");
}
```

## 🔍 Testowanie API

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

### Endpoint testowy walidacji zadań:
```bash
POST /api/validation-test/test-task
Content-Type: application/json

{
    "title": "",
    "status": "Invalid Status",
    "priority": "Invalid Priority"
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

### Tworzenie prawidłowego zadania:
```bash
POST /api/tasks
Content-Type: application/json

{
    "title": "Implementacja walidacji",
    "description": "Dodanie walidacji do projektu",
    "status": "To Do",
    "priority": "High",
    "dueDate": "2024-12-31"
}
```

## 📈 Korzyści z Walidacji

1. **Bezpieczeństwo**: Ochrona przed nieprawidłowymi danymi
2. **Jakość danych**: Gwarantuje spójność i poprawność danych
3. **User Experience**: Jasne komunikaty o błędach dla użytkowników
4. **Czytelność kodu**: Deklaratywne reguły walidacji
5. **Automatyzacja**: Automatyczna walidacja na poziomie frameworka

## 🎯 Najlepsze Praktyki

1. **Używaj odpowiednich adnotacji** dla każdego typu danych
2. **Definiuj jasne komunikaty błędów** w języku użytkownika
3. **Testuj wszystkie scenariusze walidacji** w testach jednostkowych
4. **Obsługuj błędy globalnie** za pomocą @ControllerAdvice
5. **Dokumentuj reguły walidacji** dla zespołu i użytkowników API

## 🚀 Rozszerzenia

Możliwe rozszerzenia systemu walidacji:
- Walidacja krzyżowa pól (custom validators)
- Walidacja grupowa (@Validated z grupami)
- Walidacja asynchroniczna
- Integracja z systemem logowania błędów
- Lokalizacja komunikatów błędów 