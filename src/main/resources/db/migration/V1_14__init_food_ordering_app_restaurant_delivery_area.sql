CREATE TABLE restaurant_delivery_area
(
    id  SERIAL PRIMARY KEY,
    restaurant_id      INT REFERENCES restaurant (id),
    delivery_street_id INT REFERENCES restaurant_delivery_street (id)
);