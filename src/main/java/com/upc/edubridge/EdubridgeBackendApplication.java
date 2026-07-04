package com.upc.edubridge;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
public class EdubridgeBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(EdubridgeBackendApplication.class, args);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("EduBridge API - Portal de Gestión Académica")
                        .description("Documentación interactiva de endpoints para el seguimiento y control de alumnos.")
                        .version("v4.0")
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))

                .externalDocs(new ExternalDocumentation()
                        .description("Herramienta de Auditoría: Decodificar y Validar Tokens JWT de EduBridge")
                        .url("https://jwt.io/"));
    }
}