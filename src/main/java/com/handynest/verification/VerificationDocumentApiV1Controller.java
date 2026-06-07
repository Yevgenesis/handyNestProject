package com.handynest.verification;

import com.handynest.marketplace.AttachmentMetadataRequest;
import com.handynest.marketplace.AttachmentResponse;
import com.handynest.marketplace.AttachmentUploadResponse;
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
@RequestMapping("/api/v1/verification/documents")
public class VerificationDocumentApiV1Controller {

    private final VerificationDocumentService verificationDocumentService;

    @PostMapping("/upload-url")
    @ResponseStatus(HttpStatus.CREATED)
    public AttachmentUploadResponse createUploadUrl(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AttachmentMetadataRequest request
    ) {
        return verificationDocumentService.createUploadUrl(userDetails, request);
    }

    @GetMapping
    public List<AttachmentResponse> myDocuments(@AuthenticationPrincipal UserDetails userDetails) {
        return verificationDocumentService.myDocuments(userDetails);
    }
}
