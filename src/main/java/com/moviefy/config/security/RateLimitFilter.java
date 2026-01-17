package com.moviefy.config.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moviefy.database.model.dto.response.ApiResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    private final RateLimitProperties rateLimitProperties;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final Set<String> STRICT_EMAIL_ENDPOINTS = Set.of(
            "/auth/login",
            "/auth/register",
            "/auth/password-reset/request"
    );

    public RateLimitFilter(ObjectMapper objectMapper,
                           RateLimitProperties rateLimitProperties) {
        this.objectMapper = objectMapper;
        this.rateLimitProperties = rateLimitProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest =
                new ContentCachingRequestWrapper(request);

        String path = wrappedRequest.getRequestURI();
        String method = wrappedRequest.getMethod();

        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(wrappedRequest, response);
            return;
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated =
                auth != null &&
                        auth.isAuthenticated() &&
                        !"anonymousUser".equals(auth.getPrincipal());

        String clientIp = getClientIp(wrappedRequest);

        /* =========================
           GENERAL RATE LIMIT
           ========================= */

        String generalKey = isAuthenticated
                ? "user:" + auth.getName()
                : "ip:" + clientIp;

        Bucket generalBucket = buckets.computeIfAbsent(
                generalKey,
                key -> createGeneralBucket(isAuthenticated)
        );

        if (!generalBucket.tryConsume(1)) {
            reject(response, "Too many requests. Please slow down.");
            return;
        }

        /* =========================
           STRICT AUTH ENDPOINTS
           ========================= */

        if (isStrictEndpoint(path, method)) {
            StreamUtils.copyToByteArray(wrappedRequest.getInputStream());

            // Per-IP strict
            String ipKey = "strict-ip:" + clientIp + ":" + path;
            Bucket ipBucket = buckets.computeIfAbsent(
                    ipKey,
                    key -> createStrictIpBucket(path)
            );

            if (!ipBucket.tryConsume(1)) {
                reject(response, "Too many attempts. Please try again later.");
                return;
            }

            // Per-email strict
            if (STRICT_EMAIL_ENDPOINTS.contains(path)) {
                String email = extractEmail(wrappedRequest);

                if (email != null) {
                    String emailKey = "strict-email:" + email + ":" + path;

                    Bucket emailBucket = buckets.computeIfAbsent(
                            emailKey,
                            key -> createStrictEmailBucket(path)
                    );

                    if (!emailBucket.tryConsume(1)) {
                        reject(response, "Too many attempts. Please try again later.");
                        return;
                    }
                }
            }
        }

        filterChain.doFilter(wrappedRequest, response);
    }

    /* =========================
       BUCKET BUILDERS (no deprecated APIs)
       ========================= */

    private Bucket createGeneralBucket(boolean authenticated) {
        if (authenticated) {
            int limit = rateLimitProperties.getGeneral().getAuthUser(); // per minute

            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(limit)
                                    .refillGreedy(limit, Duration.ofMinutes(1))
                                    .build()
                    )
                    .build();
        }

        int limit = rateLimitProperties.getGeneral().getAnonIp(); // per minute

        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(limit)
                                .refillGreedy(limit, Duration.ofMinutes(1))
                                .build()
                )
                .build();
    }

    private Bucket createStrictIpBucket(String path) {
        if ("/auth/login".equals(path)) {
            int limit = rateLimitProperties.getAuth().getLogin().getPerIp(); // per 10 min

            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(limit)
                                    .refillGreedy(limit, Duration.ofMinutes(10))
                                    .build()
                    )
                    .build();
        }

        if ("/auth/register".equals(path)) {
            int limit = rateLimitProperties.getAuth().getRegister().getPerIp(); // per hour

            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(limit)
                                    .refillGreedy(limit, Duration.ofHours(1))
                                    .build()
                    )
                    .build();
        }

        if ("/auth/password-reset/request".equals(path)) {
            int limit = rateLimitProperties.getAuth().getPasswordReset().getPerIp(); // per hour

            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(limit)
                                    .refillGreedy(limit, Duration.ofHours(1))
                                    .build()
                    )
                    .build();
        }

        // fallback
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(20)
                                .refillGreedy(20, Duration.ofMinutes(10))
                                .build()
                )
                .build();
    }

    private Bucket createStrictEmailBucket(String path) {
        if ("/auth/login".equals(path)) {
            int limit = rateLimitProperties.getAuth().getLogin().getPerEmail(); // per 10 min

            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(limit)
                                    .refillGreedy(limit, Duration.ofMinutes(10))
                                    .build()
                    )
                    .build();
        }

        if ("/auth/register".equals(path)) {
            int limit = rateLimitProperties.getAuth().getRegister().getPerEmail(); // per hour

            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(limit)
                                    .refillGreedy(limit, Duration.ofHours(1))
                                    .build()
                    )
                    .build();
        }

        if ("/auth/password-reset/request".equals(path)) {
            int limit = rateLimitProperties.getAuth().getPasswordReset().getPerEmail(); // per hour

            return Bucket.builder()
                    .addLimit(
                            Bandwidth.builder()
                                    .capacity(limit)
                                    .refillGreedy(limit, Duration.ofHours(1))
                                    .build()
                    )
                    .build();
        }

        // fallback
        return Bucket.builder()
                .addLimit(
                        Bandwidth.builder()
                                .capacity(10)
                                .refillGreedy(10, Duration.ofMinutes(10))
                                .build()
                )
                .build();
    }

    /* =========================
       HELPERS
       ========================= */

    private boolean isStrictEndpoint(String path, String method) {
        if (!"POST".equalsIgnoreCase(method)) {
            return false;
        }

        return "/auth/login".equals(path)
                || "/auth/register".equals(path)
                || "/auth/password-reset/request".equals(path);
    }

    private void reject(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ApiResponse<Void> body =
                ApiResponse.error(HttpStatus.TOO_MANY_REQUESTS.value(), message);

        objectMapper.writeValue(response.getWriter(), body);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractEmail(ContentCachingRequestWrapper request) {
        String emailParam = Objects.toString(request.getParameter("email"), "");
        if (!emailParam.isBlank()) {
            return normalizeEmail(emailParam);
        }

        // JSON fallback
        byte[] body = request.getContentAsByteArray();
        if (body.length == 0) {
            return null;
        }

        try {
            String json = new String(body, StandardCharsets.UTF_8);
            JsonNode root = objectMapper.readTree(json);
            JsonNode emailNode = root.get("email");

            if (emailNode == null || emailNode.isNull()) {
                return null;
            }

            String email = emailNode.asText("");
            if (email.isBlank()) {
                return null;
            }

            return normalizeEmail(email);
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
