CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    score INT DEFAULT 0,
    email VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE tracks (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    artist VARCHAR(255) NOT NULL,
    url VARCHAR(255) NOT NULL
);

CREATE TABLE quizzes (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    round_count INT NOT NULL
);

CREATE TABLE quiz_tracks (
    quiz_id BIGINT REFERENCES quizzes(id),
    track_id BIGINT REFERENCES tracks(id),
    PRIMARY KEY (quiz_id, track_id)
);