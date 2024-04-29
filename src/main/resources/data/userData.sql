CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Запись 1
INSERT INTO "user" (first_name, last_name, email, is_email_verified, password,
                    phone_number, is_phone_verified, is_passport_verified, rating, country, city)
VALUES ('John', 'Doe', 'john.doe@example.com', true, 'password123',
        '+1234567890', true, false, 'NEW', 'USA', 'New York');

-- Запись 2
INSERT INTO "user" (first_name, last_name, email, is_email_verified, password,
                    country, city)
VALUES ('Jane', 'Smith', 'jane.smith@example.com', true, 'secretpassword',
        'UK', 'London');

-- Запись 3 (неполные данные)
INSERT INTO "user" (first_name, last_name, email, is_email_verified, password)
VALUES ('Alice', 'Johnson', 'alice.johnson@example.com', false, 'password456');
