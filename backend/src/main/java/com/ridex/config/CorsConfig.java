package com.ridex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Allows the React frontend to call this API from the browser.
 *
 * Always allows localhost (for local development). Additionally allows a
 * deployed frontend origin (e.g. your Vercel URL) configured via the
 * FRONTEND_ORIGIN environment variable, so no code change is needed when
 * deploying — just set the env var on the hosting platform.
 */
@Configuration
public class CorsConfig {

    @Value("${FRONTEND_ORIGIN:}")
    private String frontendOrigin;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                java.util.List<String> origins = new java.util.ArrayList<>();
                origins.add("http://localhost:5173");
                origins.add("http://127.0.0.1:5173");

                if (frontendOrigin != null && !frontendOrigin.isBlank()) {
                    origins.add(frontendOrigin.trim());
                }

                registry.addMapping("/api/**")
                        .allowedOrigins(origins.toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }
        };
    }
}
