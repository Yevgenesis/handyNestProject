package codezilla.handynestproject;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class Main {

    public static void main(String[] args) {
        // Создание экземпляра BCryptPasswordEncoder
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // Новый пароль
        String newPassword = "peter456";

        // Хэширование нового пароля
        String encodedPassword = passwordEncoder.encode(newPassword);

        // Пример обновления записи в базе данных (псевдокод)
        String username = "alice.johnson@example.com";
        updatePasswordInDatabase(username, encodedPassword);
    }

    // Метод для обновления пароля в базе данных (замените его на реальную реализацию)
    private static void updatePasswordInDatabase(String username, String encodedPassword) {
        // Здесь должен быть код для обновления пароля в базе данных
        // Например, использование JDBC или ORM (Hibernate, JPA и т.д.)
        System.out.println("Пароль для пользователя " + username + " был обновлен на: " + encodedPassword);
    }


}
