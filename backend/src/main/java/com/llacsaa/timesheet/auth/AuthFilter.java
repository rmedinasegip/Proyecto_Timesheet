package com.llacsaa.timesheet.auth;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

/**
 * Guarda de autenticación para /api/** (registrado en {@link WebConfig}
 * solo sobre ese patrón, así que las páginas Angular en sí no pasan por
 * acá — el guard de rutas del lado del cliente es una conveniencia de UX,
 * no el límite de seguridad real, mismo criterio que el proxy.ts del
 * proyecto Next.js hermano). "/api/auth/login" y "/api/ping" (health check
 * sin datos sensibles, usado por el header antes de iniciar sesión) son
 * las únicas rutas públicas.
 */
public class AuthFilter implements Filter {

    private static final java.util.Set<String> PUBLIC_PATHS = java.util.Set.of("/api/auth/login", "/api/ping");

    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        if (PUBLIC_PATHS.contains(request.getRequestURI())) {
            chain.doFilter(req, res);
            return;
        }

        Optional<CurrentUser> user = extractToken(request).flatMap(jwtService::verify);
        if (!user.isPresent()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), Collections.singletonMap("message", "No autenticado"));
            return;
        }

        try {
            AuthContext.set(user.get());
            chain.doFilter(req, res);
        } finally {
            AuthContext.clear();
        }
    }

    private Optional<String> extractToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (JwtService.AUTH_COOKIE_NAME.equals(cookie.getName())) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
