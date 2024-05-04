INSERT INTO category (id, name, parent_id, weight)
VALUES (1, 'Ремонт', NULL, 10),
       (2, 'Строительство', NULL, 20),
       (3, 'Уборка', NULL, 30),
       (4, 'Грузоперевозки', NULL, 40),
       (5, 'Курьерские услуги', NULL, 50),
       (6, 'Сиделка', NULL, 60),
       (7, 'Репетиторство', NULL, 70),
       (8, 'Дизайн интерьера', NULL, 80),
       (9, 'Веб-разработка', NULL, 90),
       (11, 'Фотосъемка', NULL, 100),
       (12, 'Ремонт квартир', 1, 10),
       (13, 'Ремонт частных домов', 1, 20),
       (14, 'Офисный ремонт', 1, 30),
       (15, 'Установка сантехники', 1, 40),
       (16, 'Электромонтажные работы', 2, 50),
       (17, 'Косметический ремонт', 1, 60),
       (18, 'Капитальный ремонт', 1, 70),
       (19, 'Ремонт кухни', 1, 80),
       (20, 'Ремонт ванной', 1, 90),
       (21, 'Строительство домов', 2, 10),
       (22, 'Ремонт квартир под ключ', 2, 20),
       (23, 'Дачное строительство', 2, 30),
       (24, 'Внутренняя отделка', 2, 40),
       (25, 'Генеральная уборка', 3, 10),
       (26, 'Уборка после ремонта', 3, 20),
       (27, 'Перевозка мебели', 4, 10),
       (28, 'Офисный переезд', 4, 20),
       (29, 'Перевозка животных', 4, 30),
       (30, 'Доставка документов', 5, 10),
       (31, 'Доставка товаров', 5, 20),
       (32, 'Покупки с доставкой', 5, 30),
       (33, 'Уход за пожилыми людьми', 6, 10),
       (34, 'Уход за детьми', 6, 20),
       (35, 'Уход за больными', 6, 30),
       (36, 'Школьные предметы', 7, 10),
       (37, 'Школьные предметы', 7, 20),
       (38, 'Дизайн квартир', 8, 10),
       (39, 'Дизайн домов', 8, 20),
       (40, 'Дизайн офисов', 8, 30),
       (41, 'Создание сайтов', 9, 10),
       (42, 'Веб-дизайн', 9, 20),
       (43, 'Свадебная фотография', 10, 10),
       (44, 'Корпоративная фотосъемка', 10, 20),
       (45, 'Портретная фотография', 10, 30),
       (46, 'Поклейка обоев', 12, 10),
       (47, 'Укладка ламината', 12, 20),
       (48, 'Поклейка обоев', 46, 10);


INSERT INTO working_time (id, title)
VALUES (1, 'с 8 до 12'),
       (2, 'с 12 до 16'),
       (3, 'с 16 до 22'),
       (4, 'в любое время');

-- Address
INSERT INTO address (street, city, zip, country)
VALUES ('123 Unter den Linden', 'Berlin', '10117', 'Germany'),
       ('456 Königsallee', 'Düsseldorf', '40212', 'Germany'),
       ('789 Karl-Liebknecht-Strasse', 'Leipzig', '04109', 'Germany'),
       ('321 Maximilianstrasse', 'Munich', '80539', 'Germany'),
       ('555 Friedrichstrasse', 'Berlin', '10117', 'Germany'),
       ('777 Champs-Élysées', 'Paris', '75008', 'France'),
       ('888 Gran Vía', 'Madrid', '28013', 'Spain'),
       ('999 Strøget', 'Copenhagen', '1200', 'Denmark'),
       ('234 Princes Street', 'Edinburgh', 'EH2 4AD', 'United Kingdom'),
       ('678 Via del Corso', 'Rome', '00186', 'Italy');

-- User
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

-- исполнители
INSERT INTO performer (id, phone_number, is_phone_verified, is_passport_verified, description, is_available,
                       performer_rating, feedback_count, created_on, updated_on)
VALUES (1, '+49123456789', true, true, 'Опытный сантехник с большим опытом работы', true, 4.5, 100, CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP),
       (2, '+49123456789', true, true, 'Опытный маляр, предоставляю услуги качественной покраски стен', true, 4.8, 150,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (3, '+49123456789', true, true, 'Электрик с опытом работы, устанавливаю различные светильники', true, 4.0, 80,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (4, '+49123456789', true, true, 'Опытный сантехник, умею делать качественный ремонт сантехники', true, 4.7, 120,
        CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
       (5, '+49123456789', true, true, 'Опытный строитель, предоставляю услуги по строительству домов и квартир', true,
        4.2, 90, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Task
INSERT INTO task (title, description, price, task_status, is_publish, address_id, working_time_id, category_id, user_id)
VALUES ('Починить кран', 'Требуется починить кран на кухне', 50.00, 'OPEN', true, 1, 1, 12, 1),
       ('Покрасить комнату', 'Нужно покрасить стены в гостиной', 200.00, 'IN_PROGRESS', true, 2, 2, 12, 2),
       ('Установить светильники', 'Требуется установить новые светильники в коридоре', 100.00, 'COMPLETED', true, 3, 3,
        12, 3);