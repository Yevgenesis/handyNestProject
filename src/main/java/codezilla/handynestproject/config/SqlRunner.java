//package codezilla.handynestproject.config;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StreamUtils;
//
//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//
//@Component
//@RequiredArgsConstructor
//public class SqlRunner implements CommandLineRunner {
//
//    private final JdbcTemplate jdbcTemplate;
//
//    @Override
//    public void run(String... args) throws Exception {
//        executeSqlScript();
//    }
//
//    private void executeSqlScript() throws IOException {
//        ClassPathResource resource = new ClassPathResource("data/All-DataNEW.sql");
//        String sql = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
//        jdbcTemplate.execute(sql);
//    }
//}