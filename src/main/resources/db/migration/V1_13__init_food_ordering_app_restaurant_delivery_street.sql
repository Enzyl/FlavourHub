CREATE TABLE restaurant_delivery_street
(
    id          SERIAL PRIMARY KEY,
    street_name VARCHAR(255) NOT NULL,
    postal_code VARCHAR(20)  NOT NULL,
    district    VARCHAR(255) NOT NULL
);