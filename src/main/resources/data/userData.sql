-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO "user" (id, first_name, last_name, email, is_email_verified, password, created_on, updated_on, is_deleted,
                    phone_number, is_phone_verified, is_passport_verified, rating, country, city)
VALUES ('d290f1ee-6c54-4b01-90e6-d701748f0851', 'John', 'Doe', 'john.doe@example.com', true, 'password123',
        '2024-04-29 10:00:00', '2024-04-29 10:00:00', false, '+1234567890', true, true, 'STAR5', 'USA', 'New York'),
       ('067e6162-3b6f-4ae2-a171-2470b63dff00', 'Jane', 'Smith', 'jane.smith@example.com', true, 'qwerty123',
        '2024-04-29 11:00:00', '2024-04-29 11:00:00', false, '+9876543210', true, false, 'STAR4', 'Canada', 'Toronto'),
       ('db906cbb-3f6c-4bde-8e4f-63fd6484f001', 'Alice', 'Johnson', 'alice.johnson@example.com', true, 'test123',
        '2024-04-29 12:00:00', '2024-04-29 12:00:00', false, '+1122334455', true, true, 'STAR3', 'UK', 'London'),
       ('4ba15485-3f38-4d29-a2b5-859781a9f32c', 'Bob', 'Williams', 'bob.williams@example.com', true, 'password456',
        '2024-04-29 13:00:00', '2024-04-29 13:00:00', false, '+9988776655', true, false, 'STAR2', 'Australia',
        'Sydney'),
       ('f8c51260-8b38-4f78-96ff-71f81a8b439a', 'Eva', 'Brown', 'eva.brown@example.com', true, 'abc123',
        '2024-04-29 14:00:00', '2024-04-29 14:00:00', false, '+6655443322', true, true, 'STAR1', 'Germany', 'Berlin');
