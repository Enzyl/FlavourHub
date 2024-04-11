CREATE TABLE food_order
(
    id            SERIAL PRIMARY KEY,
    client_id     INT REFERENCES client (id),
    restaurant_id INT REFERENCES restaurant (id),
    order_time    TIMESTAMP NOT NULL,
    status        VARCHAR(50) NOT NULL,
    total_price   NUMERIC(10, 2) NOT NULL,
    order_number  VARCHAR(36) NOT NULL
);