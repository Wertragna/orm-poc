CREATE TABLE IF NOT EXISTS users
(
    id         BIGSERIAL,
    first_name VARCHAR(255),
    last_name  VARCHAR(255),

    CONSTRAINT users_PK PRIMARY KEY (id)
);


INSERT INTO users (first_name, last_name)
VALUES ('Bohdana', 'Sherstyniuk');

INSERT INTO users (first_name, last_name)
VALUES ('Ivan', 'Kovalenko');
