package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.example.config.JsonConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

@SpringBootTest
public class JsonConfigTest {

    @Autowired
    private ObjectMapper objectMapper;

    private TestObject testObject;

    @BeforeEach
    void setUp() {
        testObject = new TestObject();
        testObject.setId(1L);
        testObject.setName("Test Name");
        testObject.setDescription("Test Description");
        testObject.setCreatedAt(LocalDateTime.now());
        testObject.setDueDate(LocalDate.now().plusDays(7));
        testObject.setNullField(null);
    }

    @Test
    void objectMapper_ShouldBeConfiguredProperly() {
        // Given & When & Then
        assertThat(objectMapper).isNotNull();
        
        // Sprawdzenie konfiguracji Java Time Module
        assertThat(objectMapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
        
        // Sprawdzenie ignorowania nieznanych pól
        assertThat(objectMapper.isEnabled(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)).isFalse();
        
        // Sprawdzenie formatowania JSON
        assertThat(objectMapper.isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
        
        // Sprawdzenie obsługi pustych obiektów
        assertThat(objectMapper.isEnabled(SerializationFeature.FAIL_ON_EMPTY_BEANS)).isFalse();
        
        // Sprawdzenie ustawienia ignorowania null wartości
        assertThat(objectMapper.getSerializationConfig().getDefaultPropertyInclusion().getValueInclusion())
                .isEqualTo(JsonInclude.Include.NON_NULL);
    }

    @Test
    void objectMapper_ShouldSerializeDatesAsISOStrings() throws Exception {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 6, 5, 12, 30, 45);
        LocalDate date = LocalDate.of(2025, 6, 5);
        
        Map<String, Object> dateMap = Map.of(
            "dateTime", dateTime,
            "date", date
        );

        // When
        String json = objectMapper.writeValueAsString(dateMap);

        // Then
        assertThat(json).contains("2025-06-05T12:30:45");
        assertThat(json).contains("2025-06-05");
        assertThat(json).doesNotContain("timestamp");
    }

    @Test
    void objectMapper_ShouldIgnoreNullValues() throws Exception {
        // Given
        testObject.setNullField(null);

        // When
        String json = objectMapper.writeValueAsString(testObject);

        // Then
        assertThat(json).doesNotContain("nullField");
        assertThat(json).contains("name");
        assertThat(json).contains("description");
    }

    @Test
    void objectMapper_ShouldFormatJsonWithIndentation() throws Exception {
        // When
        String json = objectMapper.writeValueAsString(testObject);

        // Then
        assertThat(json).contains("\n");
        assertThat(json).contains("  "); // sprawdzenie wcięć
    }

    @Test
    void objectMapper_ShouldIgnoreUnknownPropertiesOnDeserialization() {
        // Given
        String jsonWithUnknownFields = """
            {
                "id": 1,
                "name": "Test Name",
                "unknownField1": "should be ignored",
                "unknownField2": 123,
                "description": "Test Description"
            }
            """;

        // When & Then
        assertThatCode(() -> {
            TestObject result = objectMapper.readValue(jsonWithUnknownFields, TestObject.class);
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Name");
            assertThat(result.getDescription()).isEqualTo("Test Description");
        }).doesNotThrowAnyException();
    }

    @Test
    void objectMapper_ShouldHandleDateDeserialization() throws Exception {
        // Given
        String jsonWithDates = """
            {
                "id": 1,
                "name": "Test Name",
                "createdAt": "2025-06-05T12:30:45",
                "dueDate": "2025-06-10"
            }
            """;

        // When
        TestObject result = objectMapper.readValue(jsonWithDates, TestObject.class);

        // Then
        assertThat(result.getCreatedAt()).isEqualTo(LocalDateTime.of(2025, 6, 5, 12, 30, 45));
        assertThat(result.getDueDate()).isEqualTo(LocalDate.of(2025, 6, 10));
    }

    @Test
    void objectMapper_ShouldNotFailOnEmptyObjects() {
        // Given
        EmptyTestObject emptyObject = new EmptyTestObject();

        // When & Then
        assertThatCode(() -> {
            String json = objectMapper.writeValueAsString(emptyObject);
            assertThat(json).isNotNull();
        }).doesNotThrowAnyException();
    }

    @Test
    void jsonConfig_ShouldCreatePrimaryObjectMapperBean() {
        // Given
        JsonConfig config = new JsonConfig();

        // When
        ObjectMapper mapper = config.objectMapper();

        // Then
        assertThat(mapper).isNotNull();
        assertThat(mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)).isTrue();
        assertThat(mapper.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)).isFalse();
    }

    // Klasy pomocnicze dla testów
    public static class TestObject {
        private Long id;
        private String name;
        private String description;
        private LocalDateTime createdAt;
        private LocalDate dueDate;
        private String nullField;

        // Konstruktor domyślny
        public TestObject() {}

        // Gettery i settery
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

        public LocalDate getDueDate() { return dueDate; }
        public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

        public String getNullField() { return nullField; }
        public void setNullField(String nullField) { this.nullField = nullField; }
    }

    public static class EmptyTestObject {
        // Pusty obiekt do testowania obsługi pustych beans
    }
} 