package com.usw.festival.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usw.festival.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String UNAUTHORIZED = "UNAUTHORIZED";
    private static final String LOGIN_REQUIRED_MESSAGE = "로그인이 필요합니다.";

    private final ObjectMapper objectMapper;

    public JsonAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String message = authException instanceof BadCredentialsException
                ? authException.getMessage()
                : LOGIN_REQUIRED_MESSAGE;

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(
                response.getWriter(),
                ErrorResponse.of(
                        HttpStatus.UNAUTHORIZED.value(),
                        UNAUTHORIZED,
                        message,
                        request.getRequestURI()
                )
        );
    }
}
