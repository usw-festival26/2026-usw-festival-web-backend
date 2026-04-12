package com.usw.festival.service;

import com.usw.festival.dto.admin.AdminLoginRequest;
import com.usw.festival.dto.admin.AdminLoginResponse;
import com.usw.festival.entity.AdminAccount;
import com.usw.festival.repository.AdminAccountRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminAuthService {

    private static final String INVALID_CREDENTIALS_MESSAGE = "아이디 또는 비밀번호가 올바르지 않습니다.";

    private final AdminAccountRepository adminAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextRepository securityContextRepository;
    private final CsrfTokenRepository csrfTokenRepository;
    private final SecurityContextLogoutHandler securityContextLogoutHandler;

    public AdminAuthService(AdminAccountRepository adminAccountRepository,
                            PasswordEncoder passwordEncoder,
                            SecurityContextRepository securityContextRepository,
                            CsrfTokenRepository csrfTokenRepository) {
        this.adminAccountRepository = adminAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.securityContextRepository = securityContextRepository;
        this.csrfTokenRepository = csrfTokenRepository;
        this.securityContextLogoutHandler = new SecurityContextLogoutHandler();
        this.securityContextLogoutHandler.setInvalidateHttpSession(true);
        this.securityContextLogoutHandler.setClearAuthentication(true);
    }

    public AdminLoginResponse login(AdminLoginRequest request,
                                    HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse) {
        AdminAccount adminAccount = adminAccountRepository.findByLoginId(request.loginId())
                .orElseThrow(this::badCredentials);

        if (!passwordEncoder.matches(request.password(), adminAccount.getPasswordHash())) {
            throw badCredentials();
        }

        HttpSession existingSession = httpServletRequest.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }
        httpServletRequest.getSession(true);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(createAuthentication(adminAccount));
        SecurityContextHolder.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, httpServletRequest, httpServletResponse);

        CsrfToken csrfToken = csrfTokenRepository.generateToken(httpServletRequest);
        csrfTokenRepository.saveToken(csrfToken, httpServletRequest, httpServletResponse);

        return AdminLoginResponse.from(adminAccount.getRole());
    }

    public void logout(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        securityContextLogoutHandler.logout(httpServletRequest, httpServletResponse, authentication);
        csrfTokenRepository.saveToken(null, httpServletRequest, httpServletResponse);
        expireSessionCookie(httpServletResponse);
    }

    private Authentication createAuthentication(AdminAccount adminAccount) {
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + adminAccount.getRole().name());
        return new UsernamePasswordAuthenticationToken(
                adminAccount.getLoginId(),
                null,
                List.of(authority)
        );
    }

    private void expireSessionCookie(HttpServletResponse response) {
        ResponseCookie expiredSessionCookie = ResponseCookie.from("JSESSIONID", "")
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, expiredSessionCookie.toString());
    }

    private BadCredentialsException badCredentials() {
        return new BadCredentialsException(INVALID_CREDENTIALS_MESSAGE);
    }
}
