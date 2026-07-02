package com.upc.edubridge.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:4200", 
                        "https://*.vercel.app", 
                        "http://localhost", 
                        "https://localhost", 
                        "capacitor://localhost",
                        "https://edubrigde.me",
                        "https://*.edubrigde.me",
                        "https://edubridge.me",
                        "https://*.edubridge.me"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}