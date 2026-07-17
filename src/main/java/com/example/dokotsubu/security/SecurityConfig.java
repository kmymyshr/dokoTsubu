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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
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

/**
 * Spring Securityの認証・認可・CSRF設定を集約する設定クラス。
 *
 * <p>Phase3で、独自Servlet内に分散していたログイン/ログアウト/CSRF処理をSpring Securityへ移した。
 * Phase5ではユーザー検索をDAOではなく {@link UserService} 経由に変更し、認証もService層の
 * 業務ルールを通す構成にしている。</p>
 */
@Configuration
public class SecurityConfig {
    private static final ObjectMapper OBJECT_MAPPER = ObjectMapperFactory.getObjectMapper();

    /**
     * Webアプリ全体のセキュリティルールを定義する。
     *
     * <p>JSP/Servletを残した段階移行中のため、ログイン画面や静的ファイルは許可し、
     * それ以外は認証必須にしている。REST APIとJSP画面が共存するので、エラー時の返却形式は
     * 後続のハンドラでJSON/画面遷移に分岐する。</p>
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, UserService users) throws Exception {
        // 既存JSPフォームとReact APIのどちらからでもCSRFトークンを扱えるようにする。
        HttpSessionCsrfTokenRepository csrfRepository = new HttpSessionCsrfTokenRepository();
        csrfRepository.setHeaderName("X-CSRF-Token");
        csrfRepository.setParameterName("_csrf");

        // JSP側で request attribute の _csrf を参照できるようにする。
        CsrfTokenRequestAttributeHandler csrfRequestHandler = new CsrfTokenRequestAttributeHandler();
        csrfRequestHandler.setCsrfRequestAttributeName("_csrf");

        http
                .authorizeHttpRequests(authorize -> authorize
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/", "/index.jsp", "/Login", "/Register", "/error",
                                "/favicon.ico", "/css/**", "/js/**", "/react/**").permitAll()
                        // Phase10で登録処理をReact + JSON APIへ移した。未ログインで使うが、POST時のCSRF保護は維持する。
                        .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
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

    /**
     * Spring Securityが認証時に利用するユーザー検索処理。
     *
     * <p>Phase5では、ここから直接Repository/DAOへ行かずUserServiceを通す。
     * これにより、認証に関するデータ取得の入口をService層へ揃えている。</p>
     */
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

    /** 登録時のハッシュ化とログイン時の照合で共通利用するパスワードエンコーダ。 */
    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ログイン成功時の処理。
     *
     * <p>旧Servlet/JSPでは `loginUser` をセッションに置く前提で画面が作られているため、
     * Spring Security認証後も同じ属性を設定する。Phase12ではログイン結果JSPを挟まず、
     * React化済みのメイン画面へ直接遷移させる。</p>
     */
    private AuthenticationSuccessHandler loginSuccessHandler(UserService users) {
        return (request, response, authentication) -> {
            User loginUser = users.findByName(authentication.getName());
            request.getSession().setAttribute("loginUser", loginUser);
            response.sendRedirect(request.getContextPath() + "/Main");
        };
    }

    /** ログイン失敗時はReactログイン画面へ戻し、URLパラメータでエラー表示を切り替える。 */
    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) ->
                response.sendRedirect(request.getContextPath() + "/index.jsp?error=1");
    }

    /** ログアウト後はReactログイン画面へ戻し、完了メッセージを表示できるようにする。 */
    private LogoutSuccessHandler logoutSuccessHandler() {
        return (request, response, authentication) ->
                response.sendRedirect(request.getContextPath() + "/index.jsp?logout=1");
    }

    /** 未ログイン時は、APIならJSON、画面ならログイン画面へリダイレクトする。 */
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

    /**
     * 認可エラー/CSRFエラーの返却形式を分岐する。
     *
     * <p>React側はJSONエラーを期待するため、API系エンドポイントでは画面HTMLを返さない。
     * JSP画面では通常の403として扱う。</p>
     */
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

    /** API/非同期ServletはJSONエラーを返す対象として扱う。 */
    private boolean isJsonEndpoint(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return path.startsWith("/api/") || path.equals("/FollowUser") || path.equals("/LikeMutter");
    }

    /** APIで共通利用するエラーレスポンス書き込み処理。 */
    private void writeJsonError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        OBJECT_MAPPER.writeValue(response.getWriter(), new ApiErrorResponse(status, code, message));
    }
}
