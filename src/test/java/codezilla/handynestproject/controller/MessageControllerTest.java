//package codezilla.handynestproject.controller;
//
//import codezilla.handynestproject.HandyNestProjectApplication;
//import codezilla.handynestproject.model.entity.Message;
//import codezilla.handynestproject.service.MessageService;
//import codezilla.handynestproject.testData.UserTestData;
//import codezilla.handynestproject.util.TestDatabaseConfig;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.SneakyThrows;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
//import org.springframework.transaction.annotation.Transactional;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//import java.nio.charset.StandardCharsets;
//import java.sql.Timestamp;
//import java.time.LocalDateTime;
//
//import static org.junit.Assert.assertEquals;
//import static org.junit.Assert.assertNotNull;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@Testcontainers
//@AutoConfigureMockMvc
//@ActiveProfiles("test")
//@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
//public class MessageControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private MessageService messageService;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    @Transactional
//    @SneakyThrows
//    void sendTest(){
//        Message message = Message.builder()
//                .sender(UserTestData.TEST_USER2)
//                .receiver(UserTestData.TEST_USER4)
//                .text("Test text")
//                .time(Timestamp.valueOf(LocalDateTime.now()))
//                .read(false)
//                .build();
//
//        var result = mockMvc.perform(MockMvcRequestBuilders
//                .post("/messages")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(message))
//                .characterEncoding("UTF-8"))
//                .andExpect(status().isOk())
//                .andReturn();
//        String jsonResponse = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
//        Message actualMessage = objectMapper.readValue(jsonResponse, Message.class);
//        assertNotNull(actualMessage.getId());
//        assertEquals(actualMessage.getSender(),message.getSender());
//        assertEquals(actualMessage.getReceiver(),message.getReceiver());
//    }
//
//
//
//}
