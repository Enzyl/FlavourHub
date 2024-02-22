CREATE TABLE menu
(
    id            SERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    restaurant_id INT REFERENCES restaurant (id)
);