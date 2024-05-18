//package codezilla.handynestproject.controller;
//
//import codezilla.handynestproject.dto.performer.PerformerRequestDto;
//import codezilla.handynestproject.dto.performer.PerformerResponseDto;
//import codezilla.handynestproject.service.PerformerService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.hamcrest.Matchers;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.List;
//
//import static codezilla.handynestproject.service.TestData.*;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class PerformerControllerTest {
//
//    @MockBean
//    private PerformerService performerService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Test
//    void createPerformerTest() throws Exception {
//
//        PerformerRequestDto performer = PERFORMER_REQUEST_DTO;
//        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO;
//        when(performerService.createPerformer(performer)).thenReturn(expected);
//        PerformerResponseDto actual = performerService.createPerformer(performer);
//
//        mockMvc.perform(post("/performer")
//                .content(objectMapper.writeValueAsString(performer))
//                .contentType(MediaType.APPLICATION_JSON))
//                        .andExpect(status().isOk());
//
//        assertEquals(expected, actual);
//
//    }
//
//    @Test
//    void updatePerformerTest() throws Exception {
//
//        PerformerRequestDto performer = PERFORMER_REQUEST_DTO;
//        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO;
//        when(performerService.updatePerformer(performer)).thenReturn(expected);
//        PerformerResponseDto actual = performerService.updatePerformer(performer);
//
//        mockMvc.perform(put("/performer")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(performer)))
//                .andExpect(status().isOk());
//
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    void getAllPerformersTest() throws Exception {
//
//        List<PerformerResponseDto> expected = List.of(PERFORMER_RESPONSE_DTO, PERFORMER_RESPONSE_DTO2);
//        when(performerService.getPerformers()).thenReturn(expected);
//        List<PerformerResponseDto> actual = performerService.getPerformers();
//
//        mockMvc.perform(get("/performer"))
//                .andExpect(status().isOk())
//                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
//                .andExpect(jsonPath("$", Matchers.hasSize(expected.size())));
//
//        assertEquals(expected, actual);
//    }
//
//    @Test
//    void getPerformerByIdTest() throws Exception {
//        PerformerResponseDto expected = PERFORMER_RESPONSE_DTO;
//        Long id = expected.getId();
//        when(performerService.getPerformerById(id)).thenReturn(expected);
//        PerformerResponseDto actual = performerService.getPerformerById(id);
//
//        mockMvc.perform(get("/performer/{id}",id))
//                .andExpect(status().isOk());
//
//        assertEquals(expected, actual);
//    }
//}