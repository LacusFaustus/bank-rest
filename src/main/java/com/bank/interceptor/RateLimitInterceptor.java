package com.bank.interceptor;

import com.bank.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();

        // Применяем разные лимиты для разных endpoint-ов
        RateLimitService.RateLimitType rateLimitType = getRateLimitType(endpoint);

        if (rateLimitService.isRateLimited(clientIp + "_" + endpoint, rateLimitType)) {
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Rate limit exceeded. Please try again later.");
            return false;
        }

        rateLimitService.recordRequest(clientIp + "_" + endpoint, rateLimitType);
        return true;
    }

    private RateLimitService.RateLimitType getRateLimitType(String endpoint) {
        if (endpoint.contains("/auth/login")) {
            return RateLimitService.RateLimitType.LOGIN_ATTEMPT;
        } else if (endpoint.contains("/transfer")) {
            return RateLimitService.RateLimitType.TRANSFER_OPERATION;
        } else {
            return RateLimitService.RateLimitType.API_REQUEST;
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
