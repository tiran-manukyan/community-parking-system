CREATE SCHEMA IF NOT EXISTS parking_system;

SET search_path TO 'parking_system';

CREATE TYPE booking_status AS ENUM ('BOOKED', 'USED', 'COMPLETED', 'CANCELLED');

CREATE TABLE buildings
(
    id      BIGSERIAL PRIMARY KEY,
    name    VARCHAR(255) NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE communities
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL
);

CREATE TABLE users_communities
(
    user_id      BIGINT REFERENCES users (id),
    community_id BIGINT REFERENCES communities (id),
    PRIMARY KEY (user_id, community_id)
);

CREATE TABLE communities_buildings
(
    community_id BIGINT REFERENCES communities (id),
    building_id  BIGINT REFERENCES buildings (id),
    PRIMARY KEY (community_id, building_id)
);

CREATE TABLE parking_spot
(
    id                 BIGSERIAL PRIMARY KEY,
    spot_number        INT    NOT NULL,
    building_id        BIGINT NOT NULL REFERENCES buildings (id),
    owner_community_id BIGINT REFERENCES communities (id)
);

CREATE TABLE spot_booking
(
    id           BIGSERIAL PRIMARY KEY,
    user_id      BIGINT         NOT NULL REFERENCES users (id),
    spot_id      BIGINT         NOT NULL REFERENCES parking_spot (id),
    start_time   TIMESTAMP      NOT NULL,
    end_time     TIMESTAMP,
    status       booking_status NOT NULL,
    parking_cost NUMERIC(8, 2)
);

CREATE TABLE parking_session
(
    id            BIGSERIAL PRIMARY KEY,
    user_id       BIGINT    NOT NULL REFERENCES users (id),
    spot_id       BIGINT    NOT NULL REFERENCES parking_spot (id),
    booking_id    BIGINT    NOT NULL REFERENCES spot_booking (id),
    start_time    TIMESTAMP NOT NULL,
    end_time      TIMESTAMP,
    plate_number  VARCHAR(31),
    is_pre_booked BOOLEAN   NOT NULL
);