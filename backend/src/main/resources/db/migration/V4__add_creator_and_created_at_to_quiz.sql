-- Добавление столбцов creator_id и created_at в таблицу quizzes
ALTER TABLE quizzes
    ADD COLUMN creator_id BIGINT NOT NULL,
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT now();

-- Создание внешнего ключа на таблицу пользователей (user)
ALTER TABLE quizzes
    ADD CONSTRAINT fk_quizzes_creator
    FOREIGN KEY (creator_id) REFERENCES users(id);

-- Создание таблицы для связи многие-ко-многим между quiz и track
CREATE TABLE IF NOT EXISTS quiz_tracks (
    quiz_id BIGINT NOT NULL,
    track_id BIGINT NOT NULL,
    PRIMARY KEY (quiz_id, track_id),
    CONSTRAINT fk_quiz_tracks_quiz FOREIGN KEY (quiz_id) REFERENCES quizzes(id) ON DELETE CASCADE,
    CONSTRAINT fk_quiz_tracks_track FOREIGN KEY (track_id) REFERENCES tracks(id) ON DELETE CASCADE
);