package com.handynest.notification;

import com.handynest.common.api.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationApiV1Controller {

  private final NotificationService notificationService;

  @GetMapping
  public PageResponse<NotificationResponse> myNotifications(
      @AuthenticationPrincipal UserDetails userDetails,
      @RequestParam(required = false) Boolean unreadOnly,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return notificationService.myNotifications(userDetails, unreadOnly, page, size);
  }

  @PostMapping("/{notificationId}/read")
  public NotificationResponse markRead(
      @AuthenticationPrincipal UserDetails userDetails, @PathVariable String notificationId) {
    return notificationService.markRead(userDetails, notificationId);
  }

  @PostMapping("/read-all")
  public NotificationsReadAllResponse markAllRead(
      @AuthenticationPrincipal UserDetails userDetails) {
    return new NotificationsReadAllResponse(notificationService.markAllRead(userDetails));
  }
}
