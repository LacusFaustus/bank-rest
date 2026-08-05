package com.bank.interceptor;

import com.bank.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = getClientIp(request);
        String endpoint = request.getRequestURI();

        if (endpoint == null) {
            endpoint = "";
        }

        RateLimitService.RateLimitType rateLimitType = getRateLimitType(endpoint);
        String rateLimitKey = clientIp + "_" + endpoint;

        // ИСПРАВЛЕНО: Сначала проверяем лимит
        if (rateLimitService.isRateLimited(rateLimitKey, rateLimitType)) {
            response.setStatus(429);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            try {
                response.getWriter().write("{\"error\":\"Rate limit exceeded. Please try again later.\"}");
                response.getWriter().flush();
            } catch (IOException e) {
                log.error("Error writing rate limit response", e);
            }

            log.warn("Rate limit exceeded for IP: {} on endpoint: {}", clientIp, endpoint);
            return false;
        }

        // ИСПРАВЛЕНО: Только если лимит не превышен - записываем запрос
        rateLimitService.recordRequest(rateLimitKey, rateLimitType);
        return true;
    }

    public RateLimitService.RateLimitType getRateLimitType(String endpoint) {
        if (endpoint == null) {
            return RateLimitService.RateLimitType.API_REQUEST;
        }

        if (endpoint.contains("/auth/login")) {
            return RateLimitService.RateLimitType.LOGIN_ATTEMPT;
        } else if (endpoint.contains("/transfer")) {
            return RateLimitService.RateLimitType.TRANSFER_OPERATION;
        } else {
            return RateLimitService.RateLimitType.API_REQUEST;
        }
    }

    String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}
