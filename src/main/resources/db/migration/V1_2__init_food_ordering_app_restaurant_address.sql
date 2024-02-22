CREATE TABLE restaurant_address
(
    id          SERIAL PRIMARY KEY,
    city        VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    address     TEXT         NOT NULL
);