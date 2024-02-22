CREATE TABLE owner
(
    id           SERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    surname      VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15)  NOT NULL,
    nip          VARCHAR(10)  NOT NULL,
    regon        VARCHAR(14)  NOT NULL,
    user_id      INT REFERENCES food_ordering_app_user (id)
);