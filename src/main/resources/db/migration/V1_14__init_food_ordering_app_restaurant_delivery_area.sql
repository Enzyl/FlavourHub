CREATE TABLE restaurant_delivery_area
(
    restaurant_id      INT REFERENCES restaurant (id),
    delivery_street_id INT REFERENCES restaurant_delivery_street (id),
    PRIMARY KEY (restaurant_id, delivery_street_id)
);