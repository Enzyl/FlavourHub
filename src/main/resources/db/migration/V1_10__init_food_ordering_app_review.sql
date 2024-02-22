CREATE TABLE review
(
    id          SERIAL PRIMARY KEY,
    order_id    INT REFERENCES food_order (id),
    rating      INT       NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    review_date TIMESTAMP NOT NULL
);