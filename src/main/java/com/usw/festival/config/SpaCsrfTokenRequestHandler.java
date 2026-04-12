package com.usw.festival.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

import java.util.function.Supplier;

public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

    private final CsrfTokenRequestHandler plainRequestHandler = new CsrfTokenRequestAttributeHandler();
    private final CsrfTokenRequestHandler xorRequestHandler = new XorCsrfTokenRequestAttributeHandler();

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       Supplier<CsrfToken> csrfTokenSupplier) {
        xorRequestHandler.handle(request, response, csrfTokenSupplier);
    }

    @Override
    public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
        String csrfHeaderValue = request.getHeader(csrfToken.getHeaderName());
        if (StringUtils.hasText(csrfHeaderValue)) {
            return plainRequestHandler.resolveCsrfTokenValue(request, csrfToken);
        }
        return xorRequestHandler.resolveCsrfTokenValue(request, csrfToken);
    }
}
