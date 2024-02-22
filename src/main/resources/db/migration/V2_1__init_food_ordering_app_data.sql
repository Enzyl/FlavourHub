INSERT INTO food_ordering_app_user (username, password, email, role, enabled)
VALUES ('user1', 'password1', 'user1@example.com', 'ROLE_USER', true),
       ('user2', 'password2', 'user2@example.com', 'ROLE_OWNER', true),
       ('admin', 'password3', 'admin@example.com', 'ROLE_ADMIN', true),
       ('owner4', 'pass4', 'owner4@example.com', 'ROLE_OWNER', true),
       ('owner5', 'pass5', 'owner5@example.com', 'ROLE_OWNER', true),
       ('owner6', 'pass6', 'owner6@example.com', 'ROLE_OWNER', true);
INSERT INTO restaurant_address (city, postal_code, address)
VALUES ('City1', '00001', 'Address 1'),
       ('City2', '00002', 'Address 2'),
       ('City3', '00003', 'Address 3'),
       ('City4', '004', 'Address 4'),
       ('City5', '005', 'Address 5'),
       ('City6', '006', 'Address 6');
INSERT INTO owner (name, surname, phone_number, nip, regon, user_id)
VALUES ('John', 'Doe', '123456789', '1234567890', '12345678901234', 2),
       ('Jane', 'Doe', '987654321', '0987654321', '43210987654321', 3),
       ('Owner Name 4', 'Owner Surname 4', '400-500-600', 'NIP4', 'REGON4', 4),
       ('Owner Name 5', 'Owner Surname 5', '500-600-700', 'NIP5', 'REGON5', 5),
       ('Owner Name 6', 'Owner Surname 6', '600-700-800', 'NIP6', 'REGON6', 6);
INSERT INTO restaurant (name, description, image_path, address_id, owner_id)
VALUES ('Restaurant 1', 'Description 1', '/path/to/image1.jpg', 1, 1),
       ('Restaurant 2', 'Description 2', '/path/to/image2.jpg', 2, 2),
       ('Restaurant 3', 'Description for Restaurant 3', '/path/to/image4.jpg', 3, 3),
       ('Restaurant 4', 'Description for Restaurant 4', '/path/to/image5.jpg', 4, 4),
       ('Restaurant 5', 'Description for Restaurant 5', '/path/to/image6.jpg', 5, 5);
INSERT INTO menu (name, description, restaurant_id)
VALUES ('Menu 1', 'Menu Description 1', 1),
       ('Menu 2', 'Menu Description 2', 2),
       ('Menu 3', 'Menu Description 3', 3),
       ('Menu 4', 'Menu Description 4', 4),
       ('Menu 5', 'Menu Description 5', 5);

INSERT INTO menu_item (name, description, category, price, image_path, menu_id)
VALUES ('MenuItem 1', 'Description 1', 'Category 1', 10.00, '/path/to/menuitem1.jpg', 1),
       ('MenuItem 2', 'Description 2', 'Category 2', 17.00, '/path/to/menuitem2.jpg', 2),
       ('MenuItem 3', 'Description 3', 'Category 3', 13.00, '/path/to/menuitem3.jpg', 3),
       ('MenuItem 4', 'Description 4', 'Category 4', 14.00, '/path/to/menuitem4.jpg', 4),
       ('MenuItem 5', 'Description 5', 'Category 5', 11.00, '/path/to/menuitem5.jpg', 5),
       ('MenuItem 5', 'Description 5', 'Category 5', 171.00, '/path/to/menuitem5.jpg', 1),
       ('MenuItem 6', 'Description 6', 'Category 5', 111.00, '/path/to/menuitem5.jpg', 1),
       ('MenuItem 7', 'Description 7', 'Category 5', 123.00, '/path/to/menuitem5.jpg', 2),
       ('MenuItem 8', 'Description 8', 'Category 5', 18.00, '/path/to/menuitem5.jpg', 2),
       ('MenuItem 9', 'Description 9', 'Category 5', 6.00, '/path/to/menuitem5.jpg', 3),
       ('MenuItem 10', 'Description 10', 'Category 5', 141.00, '/path/to/menuitem5.jpg', 3),
       ('MenuItem 11', 'Description 11', 'Category 5', 111.00, '/path/to/menuitem5.jpg', 3),
       ('MenuItem 12', 'Description 12', 'Category 5', 21.00, '/path/to/menuitem5.jpg', 3),
       ('MenuItem 13', 'Description 13', 'Category 5', 31.00, '/path/to/menuitem5.jpg', 4),
       ('MenuItem 14', 'Description 14', 'Category 5', 13.00, '/path/to/menuitem5.jpg', 1),
       ('MenuItem 15', 'Description 15', 'Category 6', 22.00, '/path/to/menuitem6.jpg', 5);
INSERT INTO client (full_name, phone_number, user_id)
VALUES ('Client 1', '111222333', 1),
       ('Client 2', '444555666', 2);
INSERT INTO food_order (client_id, restaurant_id, order_time, status, total_price)
VALUES (1, 1, '2023-01-01 12:00:00', 'NEW', 25.00),
       (2, 2, '2023-01-02 18:00:00', 'DELIVERED', 30.00);
INSERT INTO order_item (order_id, menu_item_id, quantity)
VALUES (1, 1, 2),
       (2, 2, 1);
INSERT INTO review (order_id, rating, comment, review_date)
VALUES (1, 5, 'Excellent!', '2023-01-02 12:34:56'),
       (2, 4, 'Very good', '2023-01-03 14:30:00');
INSERT INTO delivery (order_id, delivery_address, delivery_time, delivery_status)
VALUES (1, 'Delivery Address 1', '2023-01-01 13:00:00', 'DELIVERED'),
       (2, 'Delivery Address 2', '2023-01-02 19:30:00', 'IN TRANSIT');
INSERT INTO payment (order_id, payment_method, payment_status, payment_time, payment_amount)
VALUES (1, 'CREDIT_CARD', 'PAID', '2023-01-01 11:50:00', 25.00),
       (2, 'ONLINE', 'PAID', '2023-01-02 17:50:00', 30.00);
INSERT INTO restaurant_delivery_street (street_name, postal_code, district)
VALUES ('Street 1', '10001', 'District 1'),
       ('Street 2', '20002', 'District 2');
INSERT INTO restaurant_delivery_area (restaurant_id, delivery_street_id)
VALUES (1, 1),
       (2, 2),
       (3, 1),
       (4, 1),
       (5, 1);
