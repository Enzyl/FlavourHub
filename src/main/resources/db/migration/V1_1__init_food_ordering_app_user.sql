CREATE TABLE food_ordering_app_user
(
    id       SERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255)        NOT NULL,
    email    VARCHAR(255) UNIQUE NOT NULL,
    role     VARCHAR(50)         NOT NULL,
    enabled  BOOLEAN             NOT NULL
);