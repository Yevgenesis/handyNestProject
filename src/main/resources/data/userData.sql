-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO "user" (first_name, last_name, email, is_email_verified, password, created_on, updated_on, is_deleted,
                    phone_number, is_phone_verified, is_passport_verified, rating, country, city)
VALUES ('John', 'Doe', 'john.doe@example.com', true, 'password123',
        '2024-04-29 10:00:00', '2024-04-29 10:00:00', false, '+1234567890', true, true, 'STAR5', 'USA', 'New York'),
       ('Jane', 'Smith', 'jane.smith@example.com', true, 'qwerty123',
        '2024-04-29 11:00:00', '2024-04-29 11:00:00', false, '+9876543210', true, false, 'STAR4', 'Canada', 'Toronto'),
       ('Alice', 'Johnson', 'alice.johnson@example.com', true, 'test123',
        '2024-04-29 12:00:00', '2024-04-29 12:00:00', false, '+1122334455', true, true, 'STAR3', 'UK', 'London'),
       ('Bob', 'Williams', 'bob.williams@example.com', true, 'password456',
        '2024-04-29 13:00:00', '2024-04-29 13:00:00', false, '+9988776655', true, false, 'STAR2', 'Australia',
        'Sydney'),
       ('Eva', 'Brown', 'eva.brown@example.com', true, 'abc123',
        '2024-04-29 14:00:00', '2024-04-29 14:00:00', false, '+6655443322', true, true, 'STAR1', 'Germany', 'Berlin');
