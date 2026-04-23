# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

```bash
# Run locally (requires .env file)
./gradlew bootRun

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests "com.moviefy.SomeTestClass"

# Build JAR
./gradlew clean bootJar

# Docker
docker build -t moviefy .
docker run --rm -p 8080:8080 --env-file .env moviefy
```

Java 17, Spring Boot 3.2.5, Gradle 8.10.

## Environment Setup

Copy `.env.example` to `.env`. Required variables:
- PostgreSQL (Neon): `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- Redis (Upstash): `REDIS_URL`, `REDIS_PASSWORD`
- TMDB: `TMDB_KEY`
- Mail (Brevo SMTP): `MAIL_USERNAME`, `MAIL_PASSWORD`
- `FRONTEND_URL`, `FRONTEND_URL_LOCAL`
- Rate limit thresholds (Bucket4j): `RATE_LIMIT_*`
- `ADMIN_EMAILS` (comma-separated)

## Architecture

### Layered structure
```
web (controllers) → service (business logic) → repository (JPA) → PostgreSQL
```

### Package layout
```
com.moviefy/
├── config/          # Security, cache, REST client, rate limiting
├── database/
│   ├── model/       # JPA entities + DTOs (api, database, details, page)
│   └── repository/  # Spring Data JPA repositories
├── service/         # Interface + Impl pairs for every domain
├── web/             # REST controllers
├── utils/           # Mappers, validators, normalizers, response helpers
├── init/            # App startup initialization
└── exceptions/      # @ControllerAdvice global handler
```

### Entity hierarchy
`Media` (MappedSuperclass) → `Movie` and `TvSeries`; `TvSeries` → `SeasonTvSeries` → `EpisodeTvSeries`. DDL mode is `update` — Hibernate manages schema.

### DTO categories
- `apiDto/` — TMDB API response shapes
- `databaseDto/` — user-facing request/response objects
- `detailsDto/` — rich detail page responses
- `pageDto/` — paginated list responses
- All endpoints return `ApiResponse<T>` (status code + message + data)

### Caching (Caffeine)
Two expiry strategies:
1. **Size-bounded (100 entries):** movies/series details, trending, cast, crew, genres, collections.
2. **Calendar-expiry (Jan 1 reset):** popular and top-rated lists.

Cache invalidation is **event-driven**: services publish Spring events → domain-specific listeners evict entries. Do not call cache eviction directly; publish via `MediaEventPublisher`.

### Background jobs (all scheduled via `@Scheduled`)
| Job | Cron | Purpose |
|---|---|---|
| `MediaIngestService` | `0 0 2 * * *` | Pull new movies/series from TMDB |
| `MediaEvaluationService` | configured | Assess staleness of existing data |
| `MediaRefreshService` | configured | Update metadata for stale media |
| `AccountCleanupService` | configured | Remove expired tokens & inactive accounts |

Each pipeline has an **Orchestrator** (coordinates pages/batches) and a **Worker** (persists individual records). Orchestrators are annotated `@Async`.

### Security
- Session-based auth with `JSESSIONID` cookies; Spring Session stored in Redis.
- Email-based login (no username field).
- CSRF: `HttpSessionCsrfTokenRepository`, tokens sent via `X-XSRF-TOKEN` header.
- Rate limiting via Bucket4j (`RateLimitFilter`): per-IP and per-email buckets for login, register, and password-reset endpoints.
- CORS allows `localhost:5173`, `localhost:3000`, and the production domain with credentials.

### TMDB integration
Three service facades: `TmdbMoviesEndpointService`, `TmdbTvEndpointService`, `TmdbCommonEndpointService`. All use Spring's `RestClient`. On API errors they return `null` — callers guard against null returns before persisting.

### Testing
JUnit 5 + Mockito + WireMock (`wiremock-spring-boot`). WireMock stubs TMDB HTTP calls in integration tests. Currently thin — most test coverage lives in service-layer unit tests.
