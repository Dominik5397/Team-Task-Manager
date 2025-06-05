# Service Layer Architecture Guide

## Przegląd

Aplikacja Team Task Manager została zrefaktoryzowana w celu wprowadzenia warstwy serwisowej (Service Layer), która oddziela logikę biznesową od kontrolerów i zapewnia lepszą architekturę aplikacji.

## Architektura

### Przed refaktoryzacją
```
Controller → Repository
```

### Po refaktoryzacji
```
Controller → Service → Repository
```

## Komponenty warstwy serwisowej

### 1. TaskService

**Interface:** `TaskService`
**Implementacja:** `TaskServiceImpl`

#### Funkcjonalności:
- **CRUD Operations**: Podstawowe operacje na zadaniach
- **Change Logging**: Automatyczne logowanie zmian w zadaniach
- **Business Logic**: Przypisywanie zadań, zmiana statusu/priorytetu
- **Query Operations**: Wyszukiwanie zadań według różnych kryteriów

#### Kluczowe metody:
```java
// Podstawowe CRUD
List<Task> getAllTasks();
Optional<Task> getTaskById(Long id);
Task createTask(Task task);
Task updateTask(Long id, Task task);
void deleteTask(Long id);

// Operacje biznesowe
Task assignTaskToUser(Long taskId, Long userId);
Task unassignTask(Long taskId);
Task changeTaskStatus(Long taskId, TaskStatus newStatus);
Task changeTaskPriority(Long taskId, TaskPriority newPriority);

// Zapytania
List<Task> getTasksByUser(Long userId);
List<Task> getTasksByStatus(TaskStatus status);
List<Task> getTasksByPriority(TaskPriority priority);
```

#### Change Logging
Serwis automatycznie dodaje wpisy do loga zmian dla każdej operacji:
- **created**: Przy tworzeniu zadania
- **updated**: Przy aktualizacji zadania (z detalami zmian)
- **assigned**: Przy przypisywaniu do użytkownika
- **unassigned**: Przy usuwaniu przypisania
- **status_changed**: Przy zmianie statusu
- **priority_changed**: Przy zmianie priorytetu

### 2. UserService

**Interface:** `UserService`
**Implementacja:** `UserServiceImpl`

#### Funkcjonalności:
- **CRUD Operations**: Podstawowe operacje na użytkownikach
- **Validation**: Walidacja unikalności email i username
- **Statistics**: Obliczanie statystyk użytkownika
- **Data Seeding**: Inicjalizacja danych testowych

#### Kluczowe metody:
```java
// Podstawowe CRUD
List<User> getAllUsers();
Optional<User> getUserById(Long id);
User createUser(User user);
User updateUser(Long id, User user);
void deleteUser(Long id);

// Wyszukiwanie
Optional<User> getUserByEmail(String email);
Optional<User> getUserByUsername(String username);

// Walidacja
boolean emailExists(String email);
boolean usernameExists(String username);

// Statystyki
UserStats getUserStats(Long userId);

// Utility
void seedUsers();
List<User> getUsersWithTasks();
List<User> getUsersWithoutTasks();
```

#### Walidacja biznesowa
- Sprawdzanie unikalności email przy tworzeniu/aktualizacji
- Sprawdzanie unikalności username przy tworzeniu/aktualizacji
- Automatyczne usuwanie przypisań zadań przy usuwaniu użytkownika

### 3. UserStats

Klasa pomocnicza do przechowywania statystyk użytkownika:

```java
public class UserStats {
    private Long userId;
    private String username;
    private int totalTasks;
    private int completedTasks;
    private int inProgressTasks;
    private int todoTasks;
    private int highPriorityTasks;
    private int mediumPriorityTasks;
    private int lowPriorityTasks;
    private int overdueTasks;
    
    // Metody pomocnicze
    public double getCompletionRate();
    public int getActiveTasks();
    public boolean hasOverdueTasks();
}
```

## Kontrolery

### TaskController

Kontroler został uproszczony i deleguje wszystkie operacje do `TaskService`:

```java
@RestController
@RequestMapping("/api/tasks")
public class TaskController {
    @Autowired
    private TaskService taskService;
    
    // Podstawowe CRUD
    @GetMapping
    public List<Task> getAllTasks();
    
    @PostMapping
    public Task createTask(@Valid @RequestBody Task task);
    
    // Operacje biznesowe
    @PutMapping("/{taskId}/assign/{userId}")
    public Task assignTaskToUser(@PathVariable Long taskId, @PathVariable Long userId);
    
    @PutMapping("/{taskId}/status")
    public Task changeTaskStatus(@PathVariable Long taskId, @RequestParam TaskStatus status);
    
    // Zapytania
    @GetMapping("/user/{userId}")
    public List<Task> getTasksByUser(@PathVariable Long userId);
}
```

### UserController

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    // Podstawowe CRUD
    @GetMapping
    public List<User> getAllUsers();
    
    @PostMapping
    public User createUser(@Valid @RequestBody User user);
    
    // Operacje biznesowe
    @GetMapping("/{userId}/stats")
    public UserStats getUserStats(@PathVariable Long userId);
    
    @GetMapping("/check-email/{email}")
    public ResponseEntity<Boolean> checkEmailExists(@PathVariable String email);
}
```

## Repozytoria

### TaskRepository

Rozszerzone o metody zapytań:

```java
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByAssignedTo_Id(Long userId);
    List<Task> findByStatus(TaskStatus status);
    List<Task> findByPriority(TaskPriority priority);
    List<Task> findByAssignedTo_IdAndStatus(Long userId, TaskStatus status);
    List<Task> findByAssignedTo_IdAndPriority(Long userId, TaskPriority priority);
    
    @Query("SELECT t FROM Task t WHERE t.priority = org.example.TaskPriority.HIGH")
    List<Task> findHighPriorityTasks();
    
    @Query("SELECT t FROM Task t WHERE t.status != org.example.TaskStatus.DONE")
    List<Task> findIncompleteTasks();
    
    List<Task> findByAssignedToIsNull();
    
    @Query("SELECT t FROM Task t WHERE LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    List<Task> findByTitleContainingIgnoreCase(@Param("title") String title);
}
```

### UserRepository

```java
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    
    @Query("SELECT DISTINCT u FROM User u WHERE u.tasks IS NOT EMPTY")
    List<User> findUsersWithTasks();
    
    @Query("SELECT u FROM User u WHERE u.tasks IS EMPTY")
    List<User> findUsersWithoutTasks();
    
    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))")
    List<User> findByUsernameContainingIgnoreCase(@Param("username") String username);
}
```

## Testy jednostkowe

### TaskServiceTest

Kompleksowe testy dla `TaskServiceImpl`:
- Testowanie wszystkich metod CRUD
- Testowanie operacji biznesowych (assign, status change, etc.)
- Testowanie change logging
- Testowanie obsługi błędów

### UserServiceTest

Kompleksowe testy dla `UserServiceImpl`:
- Testowanie wszystkich metod CRUD
- Testowanie walidacji biznesowej
- Testowanie obliczania statystyk
- Testowanie seed data

## Korzyści z wprowadzenia warstwy serwisowej

### 1. Separacja odpowiedzialności
- **Kontrolery**: Obsługa HTTP, walidacja danych wejściowych
- **Serwisy**: Logika biznesowa, walidacja biznesowa
- **Repozytoria**: Dostęp do danych

### 2. Reużywalność
- Logika biznesowa może być używana przez różne kontrolery
- Łatwiejsze tworzenie nowych endpointów API

### 3. Testowalność
- Serwisy można testować niezależnie od warstwy HTTP
- Mockowanie zależności jest prostsze

### 4. Maintainability
- Logika biznesowa jest scentralizowana
- Łatwiejsze wprowadzanie zmian

### 5. Transakcyjność
- Serwisy mogą być oznaczone `@Transactional`
- Lepsze zarządzanie transakcjami bazodanowymi

## Przykłady użycia

### Tworzenie zadania z automatycznym logiem
```java
Task task = new Task();
task.setTitle("New Task");
task.setStatus(TaskStatus.TODO);
task.setPriority(TaskPriority.HIGH);

Task created = taskService.createTask(task);
// Automatycznie dodany changeLog z akcją "created"
```

### Przypisywanie zadania z logiem
```java
Task assigned = taskService.assignTaskToUser(1L, 2L);
// Automatycznie dodany changeLog z akcją "assigned"
```

### Pobieranie statystyk użytkownika
```java
UserStats stats = userService.getUserStats(1L);
System.out.println("Completion rate: " + stats.getCompletionRate() + "%");
System.out.println("Active tasks: " + stats.getActiveTasks());
```

## Endpointy API

### Task Management
- `GET /api/tasks` - Wszystkie zadania
- `POST /api/tasks` - Tworzenie zadania
- `PUT /api/tasks/{id}` - Aktualizacja zadania
- `DELETE /api/tasks/{id}` - Usuwanie zadania
- `GET /api/tasks/user/{userId}` - Zadania użytkownika
- `GET /api/tasks/status/{status}` - Zadania według statusu
- `GET /api/tasks/priority/{priority}` - Zadania według priorytetu
- `PUT /api/tasks/{taskId}/assign/{userId}` - Przypisanie zadania
- `PUT /api/tasks/{taskId}/unassign` - Usunięcie przypisania
- `PUT /api/tasks/{taskId}/status?status={status}` - Zmiana statusu
- `PUT /api/tasks/{taskId}/priority?priority={priority}` - Zmiana priorytetu

### User Management
- `GET /api/users` - Wszyscy użytkownicy
- `POST /api/users` - Tworzenie użytkownika
- `PUT /api/users/{id}` - Aktualizacja użytkownika
- `DELETE /api/users/{id}` - Usuwanie użytkownika
- `GET /api/users/{userId}/stats` - Statystyki użytkownika
- `GET /api/users/email/{email}` - Użytkownik po email
- `GET /api/users/username/{username}` - Użytkownik po username
- `GET /api/users/check-email/{email}` - Sprawdzenie istnienia email
- `GET /api/users/check-username/{username}` - Sprawdzenie istnienia username
- `GET /api/users/with-tasks` - Użytkownicy z zadaniami
- `GET /api/users/without-tasks` - Użytkownicy bez zadań
- `POST /api/users/seed` - Inicjalizacja danych testowych

## Podsumowanie

Wprowadzenie warstwy serwisowej znacznie poprawiło architekturę aplikacji:
- Lepsze oddzielenie logiki biznesowej od warstwy prezentacji
- Zwiększona testowalność i maintainability
- Scentralizowana walidacja biznesowa
- Automatyczne logowanie zmian
- Rozbudowane funkcjonalności biznesowe

Aplikacja jest teraz gotowa na dalszy rozwój i łatwe dodawanie nowych funkcjonalności. 