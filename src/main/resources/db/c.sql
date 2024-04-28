--liquibase formated sql

insert into "user" (city, country, created_on, email, first_name, is_deleted, is_email_verified, is_passport_verified,
                    is_phone_verified, last_name, password, phone_number, rating, updated_on)
values ('Berlin', 'Germany', now(), 'teu1@gmail.com', 'Alex', false, true, true, true, 'Alekseev', 'qwe12345',
        '+380674563423', 3, now()),
       ('Odessa', 'Ukraine', now(), 'teu13@gmail.com', 'Misha', false, true, true, true, 'Mihailov', 'qwe12345',
        '+3804343423', 2, now()),
       ('Hamburg', 'Germany', now(), 'teu14@gmail.com', 'Valentin', false, true, true, true, 'Strikalo', 'qwe12345',
        '+3806798883', 1, now()),
       ('Miami', 'USA', now(), 'teu16@gmail.com', 'Aleksey', false, true, true, true, 'Le', 'qwe12345', '+384567658', 6,
        now()),
       ('Miami-beatch', 'USA', now(), 'teuh16@gmail.com', 'Aleksey', false, true, true, true, 'Le', 'qwe12345',
        '+384567658', 6, now());
