-- Вставляем тестовых исполнителей
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
