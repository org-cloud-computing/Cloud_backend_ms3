package com.example.backendcarrito.config;

import org.springframework.context.annotation.*;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.web.cors.*;
import org.springframework.web.filter.CorsFilter;
import java.util.List;

@Configuration
public class CorsConfig {
    @Bean
    public FilterRegistrationBean corsFilter() {
        CorsConfiguration c = new CorsConfiguration();

        // Agregamos el dominio de API Gateway / Swagger o "*" para permitir cualquier origen
        c.setAllowedOriginPatterns(List.of(
            "https://main.d1bb82b5cdfog7.amplifyapp.com",
            "http://localhost:5173",
            "https://qjxnz9uqv9.execute-api.us-east-1.amazonaws.com", // <-- Swagger / API Gateway
            "*" // <-- Opcional: permite cualquier origen para pruebas
        ));

        c.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setMaxAge(3600L);

        var src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", c);

        var bean = new FilterRegistrationBean<>(new CorsFilter(src));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }
}
