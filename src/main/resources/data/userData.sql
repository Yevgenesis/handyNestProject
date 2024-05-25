-- CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

INSERT INTO "user" (first_name, last_name, email, is_email_verified, password, created_on, updated_on, is_deleted)
VALUES ('John', 'Doe', 'john.doe@example.com', true, 'password123',
        '2024-04-29 10:00:00', '2024-04-29 10:00:00', false),
       ('Jane', 'Smith', 'jane.smith@example.com', true, 'qwerty123',
        '2024-04-29 11:00:00', '2024-04-29 11:00:00', false),
       ('Alice', 'Johnson', 'alice.johnson@example.com', true, 'test123',
        '2024-04-29 12:00:00', '2024-04-29 12:00:00', false),
       ('Bob', 'Williams', 'bob.williams@example.com', true, 'password456',
        '2024-04-29 13:00:00', '2024-04-29 13:00:00', false),
       ('Eva', 'Brown', 'eva.brown@example.com', true, 'abc123',
        '2024-04-29 14:00:00', '2024-04-29 14:00:00', false);
