package com.example.dokotsubu.security;

import java.io.IOException;

import com.example.dokotsubu.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ApiErrorResponse;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.User;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import util.ObjectMapperFactory;

@Configuration
public class SecurityConfig {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserService users) throws Exception {
        HttpSessionCsrfTokenRepository csrfRepository = new HttpSessionCsrfTokenRepository();
        csrfRepository.setHeaderName("X-CSRF-Token");
        csrfRepository.setParameterName("_csrf");

        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/index.jsp", "/Login", "/Register", "/error",
                                "/favicon.ico", "/css/**", "/js/**", "/react/**").permitAll()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/index.jsp")
                        .loginProcessingUrl("/Login")
                        .usernameParameter("name")
                        .passwordParameter("pass")
                        .successHandler(loginSuccessHandler(users))
                        .failureHandler(loginFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutRequestMatcher(PathPatternRequestMatcher.withDefaults()
                                .matcher(HttpMethod.POST, "/Logout"))
                        .logoutSuccessHandler(logoutSuccessHandler())
                        .permitAll())
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(csrfRequestHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(this::handleUnauthenticated)
                        .accessDeniedHandler(this::handleAccessDenied));

        return http.build();
    }

    @Bean
    UserDetailsService userDetailsService(UserService users) {
        return username -> {
            User user = users.findByName(username);
            if (user == null) {
                throw new UsernameNotFoundException("ユーザーが見つかりません");
            }
            return org.springframework.security.core.userdetails.User
                    .withUsername(user.getName())
                    .password(user.getPass())
                    .roles("USER")
                    .build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private AuthenticationSuccessHandler loginSuccessHandler(UserService users) {
        return (request, response, authentication) -> {
            User loginUser = users.findByName(authentication.getName());
            request.getSession().setAttribute("loginUser", loginUser);
            request.getRequestDispatcher("/WEB-INF/jsp/loginResult.jsp").forward(request, response);
        };
    }

    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            request.setAttribute("errorMsg", "パスワードが間違っているか、ユーザーが未登録です");
            request.getRequestDispatcher("/WEB-INF/jsp/loginResult.jsp").forward(request, response);
        };
    }

    private LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) ->
                request.getRequestDispatcher("/WEB-INF/jsp/logout.jsp").forward(request, response);
    }

    private void handleUnauthenticated(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.core.AuthenticationException exception) throws IOException {
        if (isJsonEndpoint(request)) {
            writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "UNAUTHORIZED", "ログインが必要です");
            return;
        }
        response.sendRedirect(request.getContextPath() + "/");
    }

    private void handleAccessDenied(
            HttpServletRequest request,
            HttpServletResponse response,
            org.springframework.security.access.AccessDeniedException exception) throws IOException {
        if (isJsonEndpoint(request)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null
                    || !authentication.isAuthenticated()
                    || authentication instanceof AnonymousAuthenticationToken) {
                writeJsonError(response, HttpServletResponse.SC_UNAUTHORIZED,
                        "UNAUTHORIZED", "ログインが必要です");
                return;
            }
            writeJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                    "CSRF_TOKEN_INVALID", "CSRFトークンが不正です。画面を再読み込みしてください");
            return;
        }
        response.sendError(HttpServletResponse.SC_FORBIDDEN);
    }

    private boolean isJsonEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/api/") || path.equals("/FollowUser") || path.equals("/LikeMutter");
    }

    private void writeJsonError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), new ApiErrorResponse(status, code, message));
    }
}
