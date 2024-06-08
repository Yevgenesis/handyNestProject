package codezilla.handynestproject.controller;

import codezilla.handynestproject.dto.message.MessageRequestDto;
import codezilla.handynestproject.dto.message.MessageResponseDto;
import codezilla.handynestproject.mapper.MessageMapper;
import codezilla.handynestproject.service.MessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/messages")
@RequiredArgsConstructor
@Tag(name = "Message Controller", description = "Operations related to messages")
public class MessageController {

    private final MessageService messageService;
    private final MessageMapper messageMapper;

    /**
     * Sends a new message.
     *
     * @param message the message request DTO
     * @return the sent message response DTO
     */
    @Operation(summary = "Send a new message",
            description = "Sends a new message and returns the response DTO",
            security = @SecurityRequirement(name = "messages"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message successfully sent"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping
    public MessageResponseDto send(@RequestBody @Valid MessageRequestDto message) {
        return messageMapper.toMessageResponseDto(messageService.send(message));
    }

    /**
     * Marks a message as read.
     *
     * @param messageId the ID of the message to mark as read
     */
    @Operation(summary = "Mark message as read", description = "Marks a message as read by its ID",
            security = @SecurityRequirement(name = "messages"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Message successfully marked as read"),
            @ApiResponse(responseCode = "404", description = "Message not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PutMapping("/{messageId}/read")
    public void markRead(@PathVariable Long messageId) {
        messageService.markAsRead(messageId);
    }
}