package org.example.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Konfiguracja JSON dla aplikacji.
 * Centralne zarządzanie ustawieniami ObjectMapper.
 */
@Configuration
public class JsonConfig {

    /**
     * Skonfigurowany ObjectMapper jako Spring Bean.
     * Ustawienia:
     * - Obsługa Java 8 Time API
     * - Formatowanie dat jako ISO strings
     * - Ignorowanie nieznanych pól przy deserializacji
     * - Nie uwzględnianie null wartości w JSON
     * - Czytelne formatowanie JSON (wcięcia)
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        
        // Rejestracja modułu Java Time dla obsługi LocalDate, LocalDateTime, etc.
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