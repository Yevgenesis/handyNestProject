//package codezilla.handynestproject.controller;
//
//import codezilla.handynestproject.service.FeedbackService;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.test.web.servlet.MockMvc;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class FeedbackControllerTest {
//
//    @MockBean
//    private FeedbackService feedbackService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
////    @Test
////    void getAllFeedbackTest() throws Exception {
////
////        List<FeedbackResponseDto> expected = List.of(FEEDBACK_RESPONSE_DTO);
////        when(feedbackService.getAllFeedback()).thenReturn(expected);
////        List<FeedbackResponseDto> actual = feedbackService.getAllFeedback();
////
////        mockMvc.perform(get("/feedback"))
////                .andExpect(status().isOk())
////                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
////                .andExpect(content().json(objectMapper.writeValueAsString(expected)));
////
////        assertEquals("Lists should be equal", expected, actual);
////    }
//
////    @Test
////    void getFeedbackTest() throws Exception {
////        FeedbackResponseDto expected = FEEDBACK_RESPONSE_DTO;
////        Long id = expected.getId();
////        when(feedbackService.getFeedbackById(id)).thenReturn(expected);
////        FeedbackResponseDto actual = feedbackService.getFeedbackById(id);
////        mockMvc.perform(get("/feedback/{id}",id))
////                .andExpect(status().isOk());
////
////        assertEquals("Feedback should be equal", expected, actual);
////    }
//}