CREATE TABLE restaurant
(
    id                 SERIAL PRIMARY KEY,
    name               VARCHAR(255) NOT NULL,
    description        TEXT,
    image_path         TEXT,
    address_id         INT REFERENCES restaurant_address (id),
    delivery_street_id INT REFERENCES restaurant_delivery_street (id),
    owner_id           INT UNIQUE REFERENCES owner (id)
);