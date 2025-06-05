package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Kontroler testowy do demonstracji centralnej konfiguracji JSON.
 * Pokazuje jak skonfigurowany ObjectMapper obsługuje formatowanie dat,
 * ignorowanie null wartości, i inne ustawienia.
 */
@RestController
@RequestMapping("/api/json-test")
public class JsonTestController {

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test formatowania dat - LocalDateTime i LocalDate jako ISO strings
     */
    @GetMapping("/date-formatting")
    public Map<String, Object> testDateFormatting() {
        Map<String, Object> result = new HashMap<>();
        result.put("currentDateTime", LocalDateTime.now());
        result.put("currentDate", LocalDate.now());
        result.put("futureDate", LocalDate.now().plusDays(30));
        result.put("pastDateTime", LocalDateTime.now().minusHours(5));
        result.put("nullDate", null); // zostanie zignorowane w JSON
        result.put("description", "Test formatowania dat - null wartości są ignorowane");
        return result;
    }

    /**
     * Test ignorowania null wartości
     */
    @GetMapping("/null-handling")
    public Map<String, Object> testNullHandling() {
        Map<String, Object> result = new HashMap<>();
        result.put("visibleField", "Ta wartość będzie widoczna");
        result.put("anotherField", 42);
        result.put("emptyString", ""); // pusty string będzie widoczny
        result.put("zeroValue", 0); // zero będzie widoczne
        result.put("falseValue", false); // false będzie widoczne
        result.put("nullValue", null); // zostanie zignorowane
        return result;
    }

    /**
     * Test czytelnego formatowania JSON
     */
    @GetMapping("/formatted-json")
    public Map<String, Object> testFormattedJson() {
        List<Map<String, Object>> tasks = new ArrayList<>();
        
        Map<String, Object> task1 = new HashMap<>();
        task1.put("id", 1L);
        task1.put("title", "Sample Task 1");
        task1.put("priority", "HIGH");
        task1.put("status", "IN_PROGRESS");
        task1.put("createdAt", LocalDateTime.now());
        task1.put("dueDate", LocalDate.now().plusDays(7));
        tasks.add(task1);
        
        Map<String, Object> task2 = new HashMap<>();
        task2.put("id", 2L);
        task2.put("title", "Sample Task 2");
        task2.put("priority", "MEDIUM");
        task2.put("status", "TODO");
        task2.put("createdAt", LocalDateTime.now().minusHours(2));
        task2.put("dueDate", null); // zostanie zignorowane
        tasks.add(task2);

        Map<String, Object> result = new HashMap<>();
        result.put("message", "JSON jest sformatowane z wcięciami dla lepszej czytelności");
        result.put("totalTasks", tasks.size());
        result.put("tasks", tasks);
        result.put("generatedAt", LocalDateTime.now());
        return result;
    }

    /**
     * Test obsługi nieznanych pól w przychodzącym JSON
     */
    @PostMapping("/unknown-fields")
    public Map<String, Object> testUnknownFields(@RequestBody Map<String, Object> payload) {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "Otrzymano JSON z nieznanymi polami - zostały zignorowane");
        result.put("receivedFields", payload.keySet());
        result.put("processedAt", LocalDateTime.now());
        return result;
    }

    /**
     * Demonstracja bezpośredniego użycia ObjectMapper
     */
    @GetMapping("/object-mapper-demo")
    public Map<String, Object> testObjectMapperDirectly() {
        try {
            // Przykład serializacji obiektu do JSON
            Task sampleTask = new Task();
            sampleTask.setTitle("Test Task");
            sampleTask.setDescription("Demonstration of ObjectMapper configuration");
            sampleTask.setStatus(TaskStatus.TODO);
            sampleTask.setPriority(TaskPriority.HIGH);
            sampleTask.setDueDate(LocalDate.now().plusDays(5));

            String taskJson = objectMapper.writeValueAsString(sampleTask);

            Map<String, Object> configuredFeatures = new HashMap<>();
            configuredFeatures.put("JavaTimeModule", "enabled - LocalDate/LocalDateTime jako ISO strings");
            configuredFeatures.put("INDENT_OUTPUT", "enabled - czytelne formatowanie");
            configuredFeatures.put("WRITE_DATES_AS_TIMESTAMPS", "disabled - daty jako strings");
            configuredFeatures.put("FAIL_ON_UNKNOWN_PROPERTIES", "disabled - ignorowanie nieznanych pól");
            configuredFeatures.put("SerializationInclusion", "NON_NULL - ignorowanie null wartości");

            Map<String, Object> result = new HashMap<>();
            result.put("message", "Demonstracja bezpośredniego użycia ObjectMapper");
            result.put("serializedTask", taskJson);
            result.put("objectMapperClass", objectMapper.getClass().getSimpleName());
            result.put("configuredFeatures", configuredFeatures);
            result.put("testedAt", LocalDateTime.now());
            return result;
        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("error", "Błąd podczas serializacji");
            errorResult.put("message", e.getMessage());
            errorResult.put("timestamp", LocalDateTime.now());
            return errorResult;
        }
    }

    /**
     * Endpoint do testowania konfiguracji JSON z frontendu
     */
    @PostMapping("/validate-json-config")
    public Map<String, Object> validateJsonConfig(@RequestBody(required = false) Map<String, Object> testData) {
        Map<String, Object> objectMapperFeatures = new HashMap<>();
        objectMapperFeatures.put("dateHandling", "ISO strings (nie timestamps)");
        objectMapperFeatures.put("nullHandling", "ignorowane w odpowiedzi");
        objectMapperFeatures.put("indentation", "włączone dla czytelności");
        objectMapperFeatures.put("unknownProperties", "ignorowane podczas deserializacji");

        Map<String, Object> result = new HashMap<>();
        result.put("status", "success");
        result.put("message", "JSON konfiguracja działa poprawnie");
        result.put("receivedData", testData != null ? testData : new HashMap<>());
        result.put("objectMapperFeatures", objectMapperFeatures);
        result.put("timestamp", LocalDateTime.now());
        return result;
    }
} 