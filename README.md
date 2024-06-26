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


Облачная инфраструктура: 
Amazon Web Services (можно рассмотреть другие облачные платформы)
