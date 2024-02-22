CREATE TABLE payment
(
    id             SERIAL PRIMARY KEY,
    order_id       INT REFERENCES food_order (id),
    payment_method VARCHAR(50) NOT NULL,
    payment_status VARCHAR(50) NOT NULL,
    payment_time   TIMESTAMP   NOT NULL,
    payment_amount   INT   NOT NULL
);