package com.archsentinel.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig {

    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

    @Bean
    public CorsFilter corsFilter() {
        log.info("🔧 Configuring CORS filter...");

        CorsConfiguration config = new CorsConfiguration();

        // Allow credentials (cookies, authorization headers)
        config.setAllowCredentials(true);

        // Allowed origins (frontend URLs)
        config.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "https://archsentinel.netlify.app",
                "https://*.netlify.app"
        ));

        // Allowed HTTP methods
        config.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"
        ));

        // Allowed headers - Allow all headers for maximum compatibility
        config.addAllowedHeader("*");

        // Exposed headers (visible to client)
        config.setExposedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Total-Count",
                "X-Github-Event",
                "X-Hub-Signature-256"
        ));

        // Max age of preflight requests (in seconds)
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        log.info("✅ CORS configured - Allowed origins: http://localhost:*, https://archsentinel.netlify.app, https://*.netlify.app");

        return new CorsFilter(source);
    }
}

