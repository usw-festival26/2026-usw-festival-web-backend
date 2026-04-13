package com.usw.festival.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.usw.festival.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private static final String FORBIDDEN = "FORBIDDEN";
    private static final String INVALID_CSRF_TOKEN = "INVALID_CSRF_TOKEN";
    private static final String FORBIDDEN_MESSAGE = "권한이 없습니다.";
    private static final String INVALID_CSRF_TOKEN_MESSAGE = "CSRF 토큰이 유효하지 않습니다.";

    private final ObjectMapper objectMapper;

    public JsonAccessDeniedHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        boolean csrfFailure = accessDeniedException instanceof CsrfException;
        String code = csrfFailure ? INVALID_CSRF_TOKEN : FORBIDDEN;
        String message = csrfFailure ? INVALID_CSRF_TOKEN_MESSAGE : FORBIDDEN_MESSAGE;

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        objectMapper.writeValue(
                response.getWriter(),
                ErrorResponse.of(
                        HttpStatus.FORBIDDEN.value(),
                        code,
                        message,
                        request.getRequestURI()
                )
        );
    }
}
