package com.testcasesgenerator.generator.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve static files from test-reports directory
        registry.addResourceHandler("/static/**")
                .addResourceLocations("file:test-reports/")
                .setCachePeriod(3600);
        
        // Serve test reports directly
        registry.addResourceHandler("/reports/**")
                .addResourceLocations("file:test-reports/html/")
                .setCachePeriod(3600);
    }
} 