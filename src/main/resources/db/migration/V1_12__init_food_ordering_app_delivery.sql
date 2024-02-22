CREATE TABLE delivery (
                          id SERIAL PRIMARY KEY,
                          order_id INT REFERENCES food_order(id),
                          delivery_address TEXT NOT NULL,
                          delivery_time TIMESTAMP NOT NULL,
                          delivery_status VARCHAR(50) NOT NULL
);