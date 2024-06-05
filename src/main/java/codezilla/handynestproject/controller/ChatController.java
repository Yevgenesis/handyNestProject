package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Tag(name = "Chat Controller", description = "Operations related to chats")
public class ChatController {

    private final SimpMessagingTemplate simpMessagingTemplate;
    private final UserService userService;

    /**
     * Handles sending a message to a chat.
     *
     * @param message the message to send
     */
    @Operation(summary = "Send a message", description = "Send a message to the specified chat", security = @SecurityRequirement(name = "chat"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message sent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid message"),
            @ApiResponse(responseCode = "404", description = "Chat not found")
    })
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(MessageRequestDto message) {
        simpMessagingTemplate.convertAndSend("/topic/" + message.getChatId(), message);
    }

    /**
     * Handles adding a user to a chat.
     *
     * @param message the message containing the user information
     * @param headerAccessor the SimpMessageHeaderAccessor for accessing session attributes
     */
    @Operation(summary = "Add a user", description = "Add a user to the specified chat",
            security = @SecurityRequirement(name = "chat"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user information"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @MessageMapping("/chat.addUser")
    public void addUser(MessageRequestDto message, SimpMessageHeaderAccessor headerAccessor) {
        String username = userService.findById(message.getSenderId()).getFirstName();
        headerAccessor.getSessionAttributes().put("username", username);
        simpMessagingTemplate.convertAndSend("/topic/" + message.getChatId(), message);
    }
}
