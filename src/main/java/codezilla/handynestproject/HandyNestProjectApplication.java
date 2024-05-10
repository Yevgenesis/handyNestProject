package codezilla.handynestproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class HandyNestProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandyNestProjectApplication.class, args);
    }

}
