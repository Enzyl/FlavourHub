CREATE TABLE order_item
(
    id           SERIAL PRIMARY KEY,
    order_id     INT REFERENCES food_order (id),
    menu_item_id INT REFERENCES menu_item (id),
    quantity     INT            NOT NULL

);