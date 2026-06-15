package com.fb.config;

import com.fb.security.CurrentUserArgumentResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Cấu hình Web MVC cho ứng dụng
 * - CORS: cho phép tất cả origins cho API endpoints
 * - Argument Resolver: @CurrentUser annotation
 * - Resource Handler: Swagger UI resources
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;

    public WebConfig(CurrentUserArgumentResolver currentUserArgumentResolver) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
    }

    /**
     * Cấu hình CORS cho API endpoints
     * - Cho phép tất cả origins (cần cấu hình lại cho production)
     * - Hỗ trợ: GET, POST, PUT, DELETE, PATCH, OPTIONS
     * - Cho phép credentials (cookies, authorization headers)
     * - Max age: 3600s (1 giờ) cho preflight cache
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Đăng ký argument resolvers
     * Cho phép sử dụng @CurrentUser annotation trong controller methods
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    /**
     * Cấu hình resource handlers cho Swagger UI
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/swagger-ui/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/springdoc-openapi-ui-webjars/");
    }
}
