package com.aprendia.backend.security.filter;

import com.aprendia.backend.security.repository.ApiKeyRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String uri = request.getRequestURI();
        if (uri.startsWith("/swagger-ui") || uri.startsWith("/api/swagger-ui") || 
            uri.startsWith("/v3/api-docs") || uri.startsWith("/api/v3/api-docs") ||
            uri.startsWith("/error")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Permitir peticiones OPTIONS (Preflight requests de CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        // Permitir peticiones OPTIONS (Preflight requests de CORS)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null || requestApiKey.isBlank()) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing API Key");
            return;
        }

        boolean isValid = apiKeyRepository.findByKeyHash(requestApiKey)
                .map(key -> !key.isDeprecated())
                .orElse(false);

        if (!isValid) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid or Deprecated API Key");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
