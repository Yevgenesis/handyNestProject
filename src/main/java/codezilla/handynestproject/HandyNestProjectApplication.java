package codezilla.handynestproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "codezilla.handynestproject.repository")
public class HandyNestProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(HandyNestProjectApplication.class, args);
    }

}
