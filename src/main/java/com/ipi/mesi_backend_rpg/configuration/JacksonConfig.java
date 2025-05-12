package com.ipi.mesi_backend_rpg.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import java.time.format.DateTimeFormatter;

@Configuration
public class JacksonConfig {

    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    public ObjectMapper objectMapper() {
        return Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .simpleDateFormat(DATETIME_FORMAT)
                .serializers(new LocalDateSerializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .serializers(new LocalDateTimeSerializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)))
                .deserializers(new LocalDateTimeDeserializer(DateTimeFormatter.ofPattern(DATETIME_FORMAT)))
                .deserializers(new LocalDateDeserializer(DateTimeFormatter.ofPattern(DATE_FORMAT)))
                .build();
    }

    /**
     * Désérialiseur personnalisé pour LocalDateTime qui gère le format "yyyy-MM-dd HH:mm:ss"
     */
    public static class LocalDateTimeDeserializer extends com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer {
        public LocalDateTimeDeserializer(DateTimeFormatter formatter) {
            super(formatter);
        }
    }

    /**
     * Désérialiseur personnalisé pour LocalDate qui gère le format "yyyy-MM-dd"
     */
    public static class LocalDateDeserializer extends com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer {
        public LocalDateDeserializer(DateTimeFormatter formatter) {
            super(formatter);
        }
    }
}