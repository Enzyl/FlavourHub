CREATE TABLE client
(
    id           SERIAL PRIMARY KEY,
    full_name    VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15)  NOT NULL,
    user_id      INT REFERENCES food_ordering_app_user (id)
);