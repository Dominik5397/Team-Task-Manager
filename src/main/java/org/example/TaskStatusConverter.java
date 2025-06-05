package org.example;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;

/**
 * Konwertery JSON dla TaskStatus enum.
 * Umożliwiają serializację/deserializację między stringami a enumami.
 */
public class TaskStatusConverter {
    
    public static class Serializer extends JsonSerializer<TaskStatus> {
        @Override
        public void serialize(TaskStatus value, JsonGenerator gen, SerializerProvider serializers) 
                throws IOException {
            gen.writeString(value.getDisplayName());
        }
    }
    
    public static class Deserializer extends JsonDeserializer<TaskStatus> {
        @Override
        public TaskStatus deserialize(JsonParser p, DeserializationContext ctxt) 
                throws IOException {
            String value = p.getValueAsString();
            try {
                return TaskStatus.fromString(value);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(
                    "Invalid task status: " + value + 
                    ". Valid values are: To Do, In Progress, Done"
                );
            }
        }
    }
} 