-- V1: baseline schema — mirrors what Hibernate created via ddl-auto: update.
-- Uses CREATE TABLE IF NOT EXISTS so this is safe to run against an existing database.

CREATE TABLE IF NOT EXISTS collections (
    id          bigserial PRIMARY KEY,
    name        varchar(255),
    poster_path varchar(255),
    api_id      bigint NOT NULL UNIQUE,
    vote_count_average float8,
    has_movies  boolean
);

CREATE TABLE IF NOT EXISTS movies (
    id             bigserial PRIMARY KEY,
    api_id         bigint UNIQUE,
    imdb_id        varchar(255),
    overview       text,
    poster_path    varchar(255) NOT NULL,
    backdrop_path  varchar(255),
    popularity     float8,
    vote_average   float8,
    vote_count     integer,
    trailer        varchar(255),
    adult          boolean,
    ranking_year   integer,
    inserted_at    timestamp,
    updated_at     timestamp,
    refreshed_at   timestamp,
    favourite_count integer NOT NULL DEFAULT 0,
    title          varchar(255),
    original_title varchar(255),
    release_date   date,
    runtime        integer,
    collection_id  bigint REFERENCES collections (id)
);

CREATE TABLE IF NOT EXISTS tv_series (
    id                 bigserial PRIMARY KEY,
    api_id             bigint UNIQUE,
    imdb_id            varchar(255),
    overview           text,
    poster_path        varchar(255) NOT NULL,
    backdrop_path      varchar(255),
    popularity         float8,
    vote_average       float8,
    vote_count         integer,
    trailer            varchar(255),
    adult              boolean,
    ranking_year       integer,
    inserted_at        timestamp,
    updated_at         timestamp,
    refreshed_at       timestamp,
    favourite_count    integer NOT NULL DEFAULT 0,
    name               varchar(255),
    original_name      varchar(255),
    first_air_date     date,
    number_of_seasons  integer,
    number_of_episodes integer,
    status             varchar(255),
    type               varchar(255)
);

CREATE TABLE IF NOT EXISTS seasons (
    id            bigserial PRIMARY KEY,
    air_date      date,
    episode_count integer,
    season_number integer,
    poster_path   varchar(255),
    api_id        bigint UNIQUE,
    tv_series_id  bigint REFERENCES tv_series (id)
);

CREATE TABLE IF NOT EXISTS episodes (
    id             bigserial PRIMARY KEY,
    name           varchar(255),
    still_path     varchar(255),
    episode_number integer,
    season_id      bigint REFERENCES seasons (id),
    UNIQUE (season_id, episode_number)
);

CREATE TABLE IF NOT EXISTS movies_genres (
    id     bigserial PRIMARY KEY,
    name   varchar(255) UNIQUE,
    api_id bigint UNIQUE
);

CREATE TABLE IF NOT EXISTS series_genres (
    id     bigserial PRIMARY KEY,
    name   varchar(255),
    api_id bigint UNIQUE
);

CREATE TABLE IF NOT EXISTS production_companies (
    id        bigserial PRIMARY KEY,
    logo_path varchar(255),
    name      varchar(255),
    api_id    bigint UNIQUE
);

-- "cast" is a reserved SQL keyword and must be quoted
CREATE TABLE IF NOT EXISTS "cast" (
    id           bigserial PRIMARY KEY,
    name         varchar(255),
    profile_path varchar(255),
    api_id       bigint UNIQUE
);

CREATE TABLE IF NOT EXISTS crew (
    id           bigserial PRIMARY KEY,
    name         varchar(255),
    profile_path varchar(255),
    api_id       bigint UNIQUE
);

CREATE TABLE IF NOT EXISTS job_crew (
    id  bigserial PRIMARY KEY,
    job varchar(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS cast_movies (
    id        bigserial PRIMARY KEY,
    character varchar(255),
    movie_id  bigint REFERENCES movies (id),
    cast_id   bigint REFERENCES "cast" (id)
);

CREATE TABLE IF NOT EXISTS cast_tv (
    id           bigserial PRIMARY KEY,
    character    varchar(255),
    tv_series_id bigint REFERENCES tv_series (id),
    cast_id      bigint REFERENCES "cast" (id)
);

CREATE TABLE IF NOT EXISTS crew_movies (
    id       bigserial PRIMARY KEY,
    movie_id bigint REFERENCES movies (id),
    crew_id  bigint REFERENCES crew (id),
    job_id   bigint REFERENCES job_crew (id)
);

CREATE TABLE IF NOT EXISTS crew_tv (
    id           bigserial PRIMARY KEY,
    tv_series_id bigint REFERENCES tv_series (id),
    crew_id      bigint REFERENCES crew (id),
    job_id       bigint REFERENCES job_crew (id)
);

-- Many-to-many join tables
CREATE TABLE IF NOT EXISTS movie_genre (
    movie_id bigint NOT NULL REFERENCES movies (id),
    genre_id bigint NOT NULL REFERENCES movies_genres (id),
    PRIMARY KEY (movie_id, genre_id)
);

CREATE TABLE IF NOT EXISTS movie_production (
    movie_id      bigint NOT NULL REFERENCES movies (id),
    production_id bigint NOT NULL REFERENCES production_companies (id),
    PRIMARY KEY (movie_id, production_id)
);

CREATE TABLE IF NOT EXISTS series_genre (
    series_id bigint NOT NULL REFERENCES tv_series (id),
    genre_id  bigint NOT NULL REFERENCES series_genres (id),
    PRIMARY KEY (series_id, genre_id)
);

CREATE TABLE IF NOT EXISTS tv_series_production (
    series_id     bigint NOT NULL REFERENCES tv_series (id),
    production_id bigint NOT NULL REFERENCES production_companies (id),
    PRIMARY KEY (series_id, production_id)
);

CREATE TABLE IF NOT EXISTS users (
    id             bigserial PRIMARY KEY,
    first_name     varchar(20),
    last_name      varchar(20),
    email          varchar(120) NOT NULL UNIQUE,
    password_hash  varchar(120) NOT NULL,
    email_verified boolean      NOT NULL DEFAULT false,
    created_at     timestamp    NOT NULL
);

CREATE TABLE IF NOT EXISTS user_roles (
    user_id bigint      NOT NULL REFERENCES users (id),
    role    varchar(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS user_favorite_movies (
    user_id  bigint NOT NULL REFERENCES users (id),
    movie_id bigint NOT NULL REFERENCES movies (id),
    PRIMARY KEY (user_id, movie_id)
);

CREATE TABLE IF NOT EXISTS user_favorite_series (
    user_id bigint NOT NULL REFERENCES users (id),
    tv_id   bigint NOT NULL REFERENCES tv_series (id),
    PRIMARY KEY (user_id, tv_id)
);

CREATE TABLE IF NOT EXISTS email_verification_tokens (
    id         bigserial PRIMARY KEY,
    token_hash varchar(64)  NOT NULL,
    user_id    bigint       NOT NULL REFERENCES users (id),
    created_at timestamp    NOT NULL,
    expires_at timestamp    NOT NULL
);

CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id         bigserial PRIMARY KEY,
    token_hash varchar(64) NOT NULL,
    user_id    bigint      NOT NULL REFERENCES users (id),
    created_at timestamp   NOT NULL,
    expires_at timestamp   NOT NULL
);
