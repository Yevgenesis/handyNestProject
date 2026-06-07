package com.handynest.risk;

import com.handynest.identity.User;
import com.handynest.identity.RoleName;
import com.handynest.common.error.AccessDeniedBusinessException;
import com.handynest.common.error.InvalidStatusTransitionException;
import com.handynest.common.error.ResourceNotFoundException;
import com.handynest.identity.UserProfileService;
import com.handynest.marketplace.ChatMessage;
import com.handynest.moderation.ModerationCase;
import com.handynest.moderation.ModerationCaseRepository;
import com.handynest.moderation.ModerationPriority;
import com.handynest.moderation.ModerationTargetType;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RiskEventService {

    private final RiskDetectionService riskDetectionService;
    private final RiskEventRepository riskEventRepository;
    private final ModerationCaseRepository moderationCaseRepository;
    private final UserProfileService userProfileService;

    public boolean hasRisk(String text) {
        return riskDetectionService.hasRisk(text);
    }

    @Transactional
    public void recordChatMessageRisk(ChatMessage message) {
        riskDetectionService.detect(message.getText()).ifPresent(detection -> {
            riskEventRepository.save(new RiskEvent(message, detection));
            if (detection.highRisk()) {
                moderationCaseRepository.save(new ModerationCase(
                        ModerationTargetType.CHAT_MESSAGE,
                        message.getPublicId(),
                        message.getSender(),
                        "High-risk chat message: " + detection.riskType().name(),
                        priorityFor(detection.severity())
                ));
            }
        });
    }

    @Transactional(readOnly = true)
    public List<RiskEventResponse> adminEvents(
            UserDetails userDetails,
            RiskEventStatus status,
            RiskType riskType,
            RiskSeverity severity
    ) {
        assertAdmin(userDetails);
        RiskEventStatus effectiveStatus = status == null ? RiskEventStatus.OPEN : status;
        List<RiskEvent> events;
        if (riskType != null) {
            events = riskEventRepository.findAllByRiskTypeAndStatusOrderByCreatedAtDesc(riskType, effectiveStatus);
        } else if (severity != null) {
            events = riskEventRepository.findAllBySeverityAndStatusOrderByCreatedAtDesc(severity, effectiveStatus);
        } else {
            events = riskEventRepository.findAllByStatusOrderByCreatedAtDesc(effectiveStatus);
        }
        return events.stream().map(this::toResponse).toList();
    }

    @Transactional
    public RiskEventResponse resolve(
            UserDetails userDetails,
            String eventId,
            RiskEventResolutionRequest request
    ) {
        User admin = assertAdmin(userDetails);
        RiskEvent event = findEvent(eventId);
        if (event.getStatus() != RiskEventStatus.OPEN) {
            throw new InvalidStatusTransitionException(
                    "RiskEvent",
                    event.getStatus().name(),
                    RiskEventStatus.RESOLVED.name()
            );
        }
        event.resolve(admin, RiskEventStatus.RESOLVED, comment(request), Instant.now());
        return toResponse(event);
    }

    @Transactional
    public RiskEventResponse markFalsePositive(
            UserDetails userDetails,
            String eventId,
            RiskEventResolutionRequest request
    ) {
        User admin = assertAdmin(userDetails);
        RiskEvent event = findEvent(eventId);
        if (event.getStatus() != RiskEventStatus.OPEN) {
            throw new InvalidStatusTransitionException(
                    "RiskEvent",
                    event.getStatus().name(),
                    RiskEventStatus.FALSE_POSITIVE.name()
            );
        }
        event.resolve(admin, RiskEventStatus.FALSE_POSITIVE, comment(request), Instant.now());
        return toResponse(event);
    }

    private RiskEvent findEvent(String eventId) {
        return riskEventRepository.findByPublicId(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("RiskEvent", eventId));
    }

    private User assertAdmin(UserDetails userDetails) {
        User user = userProfileService.currentUser(userDetails);
        if (!user.getRoles().contains(RoleName.ADMIN)) {
            throw new AccessDeniedBusinessException("Only admins can access risk events");
        }
        return user;
    }

    private ModerationPriority priorityFor(RiskSeverity severity) {
        return switch (severity) {
            case CRITICAL -> ModerationPriority.CRITICAL;
            case HIGH -> ModerationPriority.HIGH;
            case MEDIUM -> ModerationPriority.NORMAL;
            case LOW -> ModerationPriority.LOW;
        };
    }

    private String comment(RiskEventResolutionRequest request) {
        if (request == null || request.comment() == null || request.comment().isBlank()) {
            return null;
        }
        return request.comment().trim();
    }

    private RiskEventResponse toResponse(RiskEvent event) {
        return new RiskEventResponse(
                event.getPublicId(),
                event.getUser().getPublicId(),
                event.getTask() == null ? null : event.getTask().getPublicId(),
                event.getChat() == null ? null : event.getChat().getPublicId(),
                event.getChatMessage() == null ? null : event.getChatMessage().getPublicId(),
                event.getRiskType().name(),
                event.getSeverity().name(),
                event.getDetectedText(),
                event.getNormalizedDetectedValue(),
                event.getStatus().name(),
                event.getCreatedAt(),
                event.getResolvedAt(),
                event.getResolvedByAdmin() == null ? null : event.getResolvedByAdmin().getPublicId(),
                event.getResolutionComment()
        );
    }
}
