# Moviefy

Moviefy is a production-style backend for a movie and TV discovery platform.  
It provides authenticated user flows, favorites, search/discovery APIs, and background ingestion from TMDB.

## Why I Built It

Most portfolio projects stop at CRUD. I wanted to build a backend that looks closer to a real product:

- account lifecycle (register, verify email, login, password reset)
- session-based security with CSRF and CORS controls
- abuse protection with endpoint-specific rate limits
- scheduled ingestion/refresh pipelines for media data
- persistent user personalization (favorites)

## Links

- Live app: https://www.moviefy.live
- Frontend repository: https://github.com/emilbankov/moviefy


## Key Features

- Media discovery platform with player integration via third-party sources
- Auth and account management: registration, email verification, login/logout, password reset flow
- User profile and favorites: add/remove movie and TV favorites per authenticated user
- Discovery endpoints: latest, trending, popular, top-rated, search, genre filtering
- Rich catalog relations: cast, crew, production companies, collections, seasons/episodes
- Player integration via TMDB IDs with third-party free streaming source links
- Background jobs: ingest/evaluate/refresh services for keeping local data current
- Security hardening: Bucket4j rate limits, session cookies, CSRF strategy, controlled CORS

## Architecture Highlights

- Layered architecture: `web -> service -> repository -> database`
- Spring Boot 3 + Spring Security for HTTP/security boundary
- PostgreSQL via Spring Data JPA
- Redis for Spring Session and caching support
- External provider integration with TMDB API

## My Role

I designed and implemented the backend application, including:

- domain modeling for media, people, and user entities
- authentication and email-based account verification flows
- endpoint design for discovery and user personalization
- scheduled ingestion and refresh orchestration
- production-oriented security configuration

## Tech Stack

- Java 17
- Spring Boot 3.2
- Spring Web, Spring Security, Spring Data JPA, Validation
- PostgreSQL
- Redis + Spring Session
- Bucket4j
- Gradle
- Docker

## API Samples

Health check:

```http
GET /ping
```

Register user:

```http
POST /auth/register
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "StrongPass123!",
  "username": "john"
}
```

Fetch trending movies:

```http
GET /movies/trending?page=1&size=10
```

Add a movie to favorites (authenticated):

```http
POST /users/favorites/movies/{movieId}
```

## Local Setup

Prerequisites:

- JDK 17 (`JAVA_HOME` configured)
- PostgreSQL
- Redis
- SMTP credentials

Run:

```powershell
.\gradlew.bat bootRun
```

Test:

```powershell
.\gradlew.bat test
```

Build:

```powershell
.\gradlew.bat clean bootJar
```

Docker:

```bash
docker build -t moviefy .
docker run --rm -p 8080:8080 --env-file .env moviefy
```

## Environment Variables

Core variables:

- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `REDIS_URL`
- `TMDB_API_KEY`
- `MOVIEFY_FRONTEND_URL`
- `MOVIEFY_LOGO_URL`
- `MAIL_HOST`
- `MAIL_PORT`
- `MAIL_USERNAME`
- `MAIL_PASSWORD`
- `MAIL_FROM`

Optional:

- `PORT` (default `8080`)
- `MOVIEFY_ADMIN_EMAILS`
- `MOVIEFY_RATE_LIMIT_*` (rate-limit tuning)

## Challenges and Tradeoffs

- Session auth + CSRF was chosen over JWT to keep server-side revocation and browser safety straightforward.
- Per-endpoint rate limiting was added to protect high-risk auth routes without over-throttling read APIs.
- Data freshness and API cost/performance were balanced via scheduled ingestion and refresh workers.
