# HandyNest

Онлайн-платформа, которая соединяет людей, нуждающихся в услугах, с исполнителями, готовыми их предоставить. Сервис охватывает широкий спектр задач, от бытовых поручений до профессиональных услуг.

- Поиск исполнителей: Пользователи могут искать исполнителей по категориям услуг, местоположению, цене и другим критериям.


- Размещение заказов: Пользователи могут размещать заказы, подробно описывая требуемую услугу.


- Отклики исполнителей: Исполнители могут откликаться на заказы, предлагая свои услуги и цены.


- Система рейтингов и отзывов: Заказчики и Исполнители могут оставлять друг другу отзывы после завершения задания, что помогает другим пользователям принимать взвешенные решения.

## Начало работы

Эти инструкции помогут вам запустить копию проекта на вашем локальном компьютере для целей разработки и тестирования.

### Предварительные требования

Что нужно установить на компьютер для работы с проектом:

- Java 17
- Maven 3.6.6
- Docker
- Java Spring Boot
- Spring Security
- Lombok
- Hibernate
- Liquibase



### Установка

Пошаговое руководство по установке:

```sh
# Клонируйте репозиторий
git clone https://github.com/Yevgenesis/handyNestProject.git

# Установите Maven

# для Mac Os
brew install maven

# для Linux
sudo apt update
sudo apt install maven

# для Windows
# Скачайте Maven с официального сайта.
https://maven.apache.org/download.cgi
# Распакуйте архив в удобное место, например, в C:\Program Files\Apache\maven.
# Добавьте путь к bin директории Maven в переменную окружения PATH:
# Откройте «Система» > «Дополнительные параметры системы» > «Переменные среды».
# В разделе «Системные переменные» найдите переменную Path и нажмите «Изменить».
# Добавьте новый путь, например, C:\Program Files\Apache\maven\bin.

# Запустите Docker

# Перейдите в директорию проекта
cd handyNestProject/Docker

# Запустите контейнер
docker-compose -f postgres.yml up

# Установите зависимости и соберите проект
mvn clean install

# Тестирование с помощью JaCoCo
mvn clean test
mvn jacoco:report
# Отчет о покрытии будет сгенерирован в директории target/site/jacoco

## Аутентификация для тестирования API

Для тестирования API через Postman или Swagger UI необходимо получить JWT токен.

### Получение токена

1. **Запустите приложение:**
```bash
mvn spring-boot:run
```

2. **Отправьте POST запрос для аутентификации:**

**URL:** `http://localhost:8080/users/login`

**Headers:**
```
Content-Type: application/json
```

**Body:**
```json
{
  "login": "alice.johnson@example.com",
  "password": "test123"
}
```

3. **Получите токен из ответа:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9..."
}
```

### Использование токена в Swagger UI

1. Откройте Swagger UI: `http://localhost:8080/swagger-ui-custom.html`
2. Нажмите кнопку **"Authorize"** (🔒) в правом верхнем углу
3. В поле **"Value"** вставьте: `Bearer YOUR_TOKEN_HERE`
4. Нажмите **"Authorize"**

### Использование токена в Postman

1. В заголовках запроса добавьте:
```
Authorization: Bearer YOUR_TOKEN_HERE
```

2. Или в настройках коллекции:
   - Перейдите в **Settings** → **Authorization**
   - Выберите тип **Bearer Token**
   - Вставьте токен в поле **Token**

### Примеры запросов

**cURL:**
```bash
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{"login": "alice.johnson@example.com", "password": "test123"}'
```

**HTTPie:**
```bash
http POST localhost:8080/users/login \
  login=alice.johnson@example.com \
  password=test123
```

### Другие тестовые пользователи

В системе также доступны другие пользователи для тестирования:
- `bob.smith@example.com` / `test123`
- `carol.wilson@example.com` / `test123`

## Облачная инфраструктура

Amazon Web Services (можно рассмотреть другие облачные платформы)
