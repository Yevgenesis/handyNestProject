package com.handynest.verification;

import com.handynest.performer.PerformerVerificationLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record VerificationRequestCreateRequest(
        @NotNull PerformerVerificationLevel requestedLevel,
        @NotEmpty List<@NotEmpty String> documentIds,
        @Size(max = 1000) String comment
) {
    public VerificationRequestCreateRequest {
        documentIds = documentIds == null ? List.of() : List.copyOf(documentIds);
    }
}
