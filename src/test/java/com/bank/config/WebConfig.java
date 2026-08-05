package com.bank.config;

import com.bank.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@Profile("!test") // Исключаем из тестов
public class WebConfig implements WebMvcConfigurer {

    private final RateLimitInterceptor rateLimitInterceptor;

    @Value("${app.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (rateLimitEnabled) {
            registry.addInterceptor(rateLimitInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns("/h2-console/**", "/error", "/actuator/**");
        }
    }
}
