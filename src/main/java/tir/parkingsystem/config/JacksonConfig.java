package tir.parkingsystem.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tir.parkingsystem.util.CustomInstantDeserializer;
import tir.parkingsystem.util.CustomInstantSerializer;

import java.time.Instant;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer customizer() {
        return builder -> builder
                .serializerByType(Instant.class, new CustomInstantSerializer())
                .deserializerByType(Instant.class, new CustomInstantDeserializer());
    }
}
