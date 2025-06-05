package org.example;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Konwertery JSON dla TaskPriority enum.
 * Umożliwiają serializację/deserializację między stringami a enumami.
 */
public class TaskPriorityConverter {
    
    public static class Serializer extends JsonSerializer<TaskPriority> {
        @Override
        public void serialize(TaskPriority value, JsonGenerator gen, SerializerProvider serializers) 
                throws IOException {
            gen.writeString(value.getDisplayName());
        }
    }
    
    public static class Deserializer extends JsonDeserializer<TaskPriority> {
        @Override
        public TaskPriority deserialize(JsonParser p, DeserializationContext ctxt) 
                throws IOException {
            String value = p.getValueAsString();
            try {
                return TaskPriority.fromString(value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid task priority: " + value + 
                    ". Valid values are: Low, Medium, High"
                );
            }
        }
    }
} 