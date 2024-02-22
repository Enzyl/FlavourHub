CREATE TABLE menu_item
(
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(255)   NOT NULL,
    description TEXT,
    category    VARCHAR(255)   NOT NULL,
    price       NUMERIC(10, 2) NOT NULL,
    image_path  TEXT,
    menu_id     INT REFERENCES menu (id)

);