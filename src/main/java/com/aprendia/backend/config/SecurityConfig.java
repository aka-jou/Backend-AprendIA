package com.aprendia.backend.config;

import com.aprendia.backend.common.response.ApiResponseWriter;
import com.aprendia.backend.feature.auth.utils.JwtAuthenticationFilter;
import com.aprendia.backend.security.filter.ApiKeyFilter;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private com.aprendia.backend.security.filter.ApiKeyFilter apiKeyFilter;

    @Autowired
    private ApiResponseWriter apiResponseWriter;

    @Value("${app.security.cors.allowed-origins:http://localhost:3000,http://localhost:5173,http://localhost:8080,http://localhost:4200,https://45.85.249.128,http://45.85.249.128}")
    private String allowedOrigins;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Habilitar CORS y deshabilitar CSRF (no es necesario con tokens JWT stateless)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())

                // 401 (sin token / token inválido) y 403 (sin rol suficiente) con el envelope estándar
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint((request, response, authException) ->
                                apiResponseWriter.writeError(response, HttpStatus.UNAUTHORIZED,
                                        "No autorizado para acceder a este recurso. Inicie sesión."))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                apiResponseWriter.writeError(response, HttpStatus.FORBIDDEN,
                                        "No tiene permisos para acceder a este recurso."))
                )

                // Sesiones sin estado
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Reglas de autorización
                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas de autenticación (login admin y student no requieren token)
                        .requestMatchers("/v1/auth/admin", "/v1/auth/student").permitAll()

                        // Registro de usuarios público (no requiere token)
                        .requestMatchers("/v1/users/register").permitAll()

                        // Swagger UI y OpenAPI docs públicos
                        .requestMatchers(
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/swagger-ui/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/v3/api-docs/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v3/api-docs/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/swagger-ui.html"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui.html"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/webjars/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api/webjars/**"),
                                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/error")
                        ).permitAll()

                        // Actuator Health Check público (útil para docker-compose healthchecks)
                        .requestMatchers("/api/actuator/health", "/actuator/health", "/health", "/api/health", "/v1/health", "/api/v1/health").permitAll()

                        .anyRequest().authenticated()
                );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Parsear la lista de orígenes autorizados desde variables de entorno
        List<String> originsList = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .toList();
        
        configuration.setAllowedOrigins(originsList);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Cache-Control", "Accept", "X-API-KEY"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // 1 hora de caché para peticiones preflight OPTIONS

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public FilterRegistrationBean<ApiKeyFilter> apiKeyFilterRegistration(ApiKeyFilter apiKeyFilter) {
        // ApiKeyFilter es un @Component que extiende OncePerRequestFilter: Spring Boot lo auto-registra
        // como filtro global del contenedor de servlets (todas las rutas), sin pasar por las reglas de
        // permitAll() de este SecurityConfig. Aquí se desactiva ese auto-registro; si en el futuro se
        // quiere validar API key en rutas concretas, se agrega explícitamente con
        // http.addFilterBefore(apiKeyFilter, ...) dentro de filterChain(), igual que jwtAuthenticationFilter.
        FilterRegistrationBean<ApiKeyFilter> registration = new FilterRegistrationBean<>(apiKeyFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    public org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/v3/api-docs/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/swagger-ui.html"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/webjars/**"),
                new org.springframework.security.web.util.matcher.AntPathRequestMatcher("/api-docs/**")
        );
    }
}
