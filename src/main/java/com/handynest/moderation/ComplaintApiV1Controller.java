package com.handynest.moderation;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ComplaintApiV1Controller {

    private final ModerationService moderationService;

    @PostMapping("/complaints")
    @ResponseStatus(HttpStatus.CREATED)
    public ComplaintResponse create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ComplaintCreateRequest request
    ) {
        return moderationService.createComplaint(userDetails, request);
    }

    @GetMapping("/my/complaints")
    public List<ComplaintResponse> myComplaints(@AuthenticationPrincipal UserDetails userDetails) {
        return moderationService.myComplaints(userDetails);
    }
}
