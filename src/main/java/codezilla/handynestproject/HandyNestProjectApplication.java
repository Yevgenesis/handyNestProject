package codezilla.handynestproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"codezilla.handynestproject", "com.handynest"})
@EntityScan(basePackages = {"codezilla.handynestproject.model.entity", "com.handynest"})
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = {"codezilla.handynestproject.repository", "com.handynest"})
@EnableScheduling
public class HandyNestProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandyNestProjectApplication.class, args);
    }

}
