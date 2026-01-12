package com.moviefy.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviefy.database.model.dto.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {
    private final ObjectMapper objectMapper;

    @Value("${moviefy.frontend.url}")
    private String frontendUrl;

    public SecurityConfig(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        HttpSessionCsrfTokenRepository repo = new HttpSessionCsrfTokenRepository();
        repo.setHeaderName("X-XSRF-TOKEN");

        http
                .csrf(csrf -> csrf
                        .csrfTokenRepository(repo)
                        .ignoringRequestMatchers("/auth/login", "/auth/logout")
                )
                .addFilterAfter(new CsrfHeaderFilter(), CsrfFilter.class)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET,
                                "/movies/**",
                                "/series/**",
                                "/all/**",
                                "/cast/**",
                                "/crew/**",
                                "/prod/**",
                                "/ping"
                        ).permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )

                .formLogin(form -> form
                        .loginProcessingUrl("/auth/login")
                        .usernameParameter("email")
                        .passwordParameter("password")

                        .successHandler((req, res, auth) -> {
                            res.setStatus(HttpStatus.OK.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ApiResponse<Void> body = ApiResponse.success(HttpStatus.OK.value(), "ok", null);
                            objectMapper.writeValue(res.getWriter(), body);
                        })

                        .failureHandler((req, res, ex) -> {
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ApiResponse<Void> body;
                            int status;

                            if (ex instanceof DisabledException) {
                                status = HttpStatus.FORBIDDEN.value();
                                body = ApiResponse.error(status, "Please verify your email before logging in.");
                            } else if (ex instanceof LockedException) {
                                status = HttpStatus.LOCKED.value();
                                body = ApiResponse.error(status, "Your account is locked.");
                            } else if (ex instanceof BadCredentialsException) {
                                status = HttpStatus.UNAUTHORIZED.value();
                                body = ApiResponse.error(status, "Invalid email or password.");
                            } else {
                                status = HttpStatus.UNAUTHORIZED.value();
                                body = ApiResponse.error(status, "Authentication failed.");
                            }

                            res.setStatus(status);
                            objectMapper.writeValue(res.getWriter(), body);
                        })
                )

                .logout(logout -> logout
                        .logoutUrl("/auth/logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("SESSION", "JSESSIONID")
                        .logoutSuccessHandler((req, res, auth) -> {
                            res.setStatus(HttpStatus.OK.value());
                            res.setContentType(MediaType.APPLICATION_JSON_VALUE);

                            ApiResponse<Void> body = ApiResponse.success(HttpStatus.OK.value(), "logged out", null);
                            objectMapper.writeValue(res.getWriter(), body);
                        })
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowCredentials(true);

        cfg.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://localhost:3000",
                frontendUrl
        ));

        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("Content-Type", "Authorization", "X-Requested-With", "X-XSRF-TOKEN"));
        cfg.setExposedHeaders(List.of("X-XSRF-TOKEN"));

        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer s = new DefaultCookieSerializer();
        s.setCookieName("SESSION");
        s.setCookiePath("/");
        s.setUseHttpOnlyCookie(true);

        s.setSameSite("None");
        s.setUseSecureCookie(true);

        return s;
    }
}