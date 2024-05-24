INSERT INTO category (id, title, parent_id, weight)
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
INSERT INTO handy_user (first_name, last_name, email, is_email_verified, password, created_on, updated_on, is_deleted,
                        task_count, user_rating)
VALUES ('Джон', 'Доу', 'john.doe@example.com', true, 'password123',
        '2024-04-29 10:00:00', '2024-04-29 10:00:00', false, 5, 100.0),
       ('Джейн', 'Смит', 'jane.smith@example.com', true, 'qwerty123',
        '2024-04-29 11:00:00', '2024-04-29 11:00:00', false, 6, 50.0),
       ('Алиса', 'Джонсон', 'alice.johnson@example.com', true, 'test123',
        '2024-04-29 12:00:00', '2024-04-29 12:00:00', false, 7,80.0),
       ('Боб', 'Уильямс', 'bob.williams@example.com', true, 'password456',
        '2024-04-29 13:00:00', '2024-04-29 13:00:00', false, 8, 75.0),
       ('Ева', 'Браун', 'eva.brown@example.com', true, 'abc123',
        '2024-04-29 14:00:00', '2024-04-29 14:00:00', false, 9,95.0),
       ('Иван', 'Иванов', 'ivan.ivanov@example.com', true, 'ivan123',
        '2024-05-01 10:00:00', '2024-05-01 10:00:00', false, 10,85.0),
       ('Мария', 'Петрова', 'maria.petrova@example.com', true, 'maria456',
        '2024-05-01 11:00:00', '2024-05-01 11:00:00', false, 11,80.0),
       ('Александр', 'Сидоров', 'alex.sid@example.com', true, 'sid123',
        '2024-05-01 12:00:00', '2024-05-01 12:00:00', false, 12, 100),
       ('Ольга', 'Смирнова', 'olga.sm@example.com', true, 'olga789',
        '2024-05-01 13:00:00', '2024-05-01 13:00:00', false, 13,80.0),
       ('Петр', 'Кузнецов', 'peter.kuz@example.com', true, 'peter456',
        '2024-05-01 14:00:00', '2024-05-01 14:00:00', false, 14,65.0),
       ('Светлана', 'Иванова', 'svetlana.ivanova@example.com', true, 'sveta123',
        '2024-05-01 15:00:00', '2024-05-01 15:00:00', false, 15,100);

-- исполнители
INSERT INTO performer (id, phone_number, is_phone_verified, is_passport_verified, description, is_available,
                       performer_rating, task_count, created_on, updated_on, address_id)
VALUES (1, '+49123456789', true, true, 'Опытный сантехник с большим опытом работы', true, 4.5, 100,
        '2024-04-29 12:00:00', '2024-04-29 12:00:00', 1),
       (2, '+49123456789', true, true, 'Опытный маляр, предоставляю услуги качественной покраски стен', true, 4.8, 150,
        '2024-04-29 13:00:00', '2024-04-29 13:00:00', 2),
       (3, '+49123456789', true, true, 'Электрик с опытом работы, устанавливаю различные светильники', true, 4.0, 80,
        '2024-04-29 14:00:00', '2024-04-29 14:00:00', 3),
       (4, '+49123456789', true, true, 'Опытный сантехник, умею делать качественный ремонт сантехники', true, 4.7, 120,
        '2024-04-29 15:00:00', '2024-04-29 15:00:00', 4),
       (5, '+49123456789', true, true, 'Опытный строитель, предоставляю услуги по строительству домов и квартир', true,
        4.2, 90, '2024-04-29 16:00:00', '2024-04-29 16:00:00', 5);

-- категории для Performer
INSERT INTO performer_categories(category_id, performer_id)
VALUES (2, 1),
       (4, 1),
       (8, 2),
       (19, 2),
       (20, 2),
       (8, 3),
       (18, 4),
       (35, 4),
       (40, 5),
       (28, 5);


-- Task
INSERT INTO task (title, description, price, task_status, is_publish, address_id, working_time_id, category_id, user_id,
                  performer_id, created_on, updated_on)
VALUES
    ('Починить кран', 'Требуется починить кран на кухне', 50.00, 'OPEN', true, 1, 1, 19, 5, NULL,
     '2024-05-14 12:00:00', '2024-05-14 12:00:00'),
    ('Покрасить комнату', 'Нужно покрасить стены в гостиной', 200.00, 'IN_PROGRESS', true, 2, 2, 8, 3, 4,
     '2024-05-14 12:00:00', '2024-05-14 12:00:00'),
    ('Установить светильники', 'Требуется установить новые светильники в коридоре', 100.00, 'COMPLETED', true, 3, 3, 2, 3, 5,
     '2024-05-14 12:00:00', '2024-05-14 12:00:00'),
    ('Офисный переезд', 'Требуется упаковать мебель и вещи', 500.00, 'OPEN', true, 4, 4, 28, 2, 3,
     '2024-05-14 12:00:00', '2024-05-14 12:00:00'),
    ('Установить светильники', 'Хочу установить старые светильники в сарае', 100.00, 'CANCELED', true, 5, 4, 18, 1, 2,
     '2024-05-14 12:00:00', '2024-05-14 12:00:00'),
    ('Установить люстру', 'Хочу установить новую люстру в туалете', 100.00, 'COMPLETED', true, 5, 4, 18, 1, 3,
     '2024-05-14 12:00:00', '2024-05-14 12:00:00'),
    ('Разбить люстру', 'Хочу разбить новую люстру в туалете', 100.00, 'COMPLETED', true, 5, 4, 18, 1, 3,
     '2024-05-14 12:00:00', '2024-05-14 12:00:00'),
    ('Починить дверь', 'Требуется починить дверь в ванной', 75.00, 'IN_PROGRESS', true, 2, 3, 17, 3, 1,
     '2024-05-15 09:00:00', '2024-05-15 09:00:00'),
    ('Покрасить забор', 'Нужно покрасить забор на даче', 150.00, 'COMPLETED', true, 1, 1, 19, 4, 2,
     '2024-05-15 10:00:00', '2024-05-15 10:00:00'),
    ('Установить розетки', 'Требуется установить розетки в комнате', 60.00, 'IN_PROGRESS', true, 2, 2, 8, 5, 3,
     '2024-05-15 11:00:00', '2024-05-15 11:00:00'),
    ('Переставить мебель', 'Нужно переставить мебель в квартире', 80.00, 'COMPLETED', true, 3, 3, 2, 6, 4,
     '2024-05-15 12:00:00', '2024-05-15 12:00:00'),
    ('Собрать мебель', 'Требуется собрать новую мебель из Икеи', 120.00, 'OPEN', true, 6, 1, 19, 7, NULL,
     '2024-05-16 09:00:00', '2024-05-16 09:00:00'),
    ('Починить потолок', 'Требуется починить потолок после протечки', 300.00, 'COMPLETED', true, 7, 3, 17, 8, 3,
     '2024-05-16 10:00:00', '2024-05-16 10:00:00'),
    ('Установить лестницу', 'Требуется сломать потолок и установить лестницу на второй этаж', 1000.00, 'COMPLETED', true, 7, 3, 17, 4, 3,
     '2024-05-16 10:00:00', '2024-05-16 10:00:00'),
    ('Почистить ковры', 'Нужно почистить ковры в квартире', 80.00, 'OPEN', true, 8, 4, 26, 9, NULL,
     '2024-05-16 11:00:00', '2024-05-16 11:00:00');




-- Feedback
INSERT INTO feedback (sender_id, text, grade, task_id, created_on)
VALUES
    (3, 'Хреновый заказчик! Не угостил чаем', 3, 4, '2024-05-14 12:00:00'),
    (2, 'Заказчица бомба!', 4, 5, '2024-05-14 12:15:00'),
    (4, NULL, 3, 2, '2024-05-14 12:30:00'),
    (5, 'Отличный заказчик!', 5, 3, '2024-05-14 12:30:00'),
    (3, 'Требуется улучшение', 1, 2, '2024-05-14 13:00:00'),
    (2, 'Ворюга! Украл пачку чая!', 1, 4, '2024-05-14 12:30:00'),
    (1, 'Отличный исполнитель!', 2, 6, '2024-05-14 12:30:00'),
    (1, 'Щедрый исполнитель, не взял деньги', 5, 7, '2024-05-14 12:30:00'),
    (1, 'Заказчик был доволен!', 4, 8, '2024-05-15 10:30:00'),
    (4, 'Работа выполнена качественно', 5, 9, '2024-05-15 11:30:00'),
    (3, 'Быстрая оплата от клиента, лайк!', 4, 10, '2024-05-15 12:30:00'),
    (4, 'Хороший заказчик', 5, 11, '2024-05-15 13:30:00');


