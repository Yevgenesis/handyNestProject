package com.handynest.notification;

import static org.assertj.core.api.Assertions.assertThat;

import com.handynest.HandyNestProjectApplication;
import com.handynest.identity.User;
import com.handynest.identity.UserRepository;
import com.handynest.testsupport.TestDatabaseConfig;
import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@ContextConfiguration(classes = {TestDatabaseConfig.class, HandyNestProjectApplication.class})
class OutboxEventWorkerTest {

  @Autowired private DomainEventPublisher publisher;

  @Autowired private OutboxEventWorker worker;

  @Autowired private OutboxEventRepository outboxRepository;

  @Autowired private NotificationRepository notificationRepository;

  @Autowired private UserRepository userRepository;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Test
  void createsExactlyOneNotificationPerEventAndRecipient() {
    User recipient = userRepository.findAll().stream().findFirst().orElseThrow();
    OutboxEvent event =
        publisher.publish(
            DomainEventType.TASK_PUBLISHED,
            "Task",
            "06TESTOUTBOX000000000000001",
            recipient,
            NotificationType.TASK_PUBLISHED,
            "Task published",
            "Task is visible",
            "Task",
            "06TESTOUTBOX000000000000001",
            Map.of());

    worker.processDueEvents();
    worker.processDueEvents();

    assertThat(
            notificationRepository.existsBySourceEventIdAndUserId(event.getId(), recipient.getId()))
        .isTrue();
    Integer count =
        jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM notification WHERE source_event_id = ? AND user_id = ?",
            Integer.class,
            event.getId(),
            recipient.getId());
    assertThat(count).isEqualTo(1);
    assertThat(outboxRepository.findById(event.getId()).orElseThrow().getStatus())
        .isEqualTo(OutboxStatus.PROCESSED);
  }

  @Test
  void retriesBrokenEventAndMovesItToDeadLetter() {
    OutboxEvent event =
        outboxRepository.save(
            new OutboxEvent(
                DomainEventType.CHAT_MESSAGE_CREATED.name(),
                "Chat",
                "06TESTOUTBOX000000000000002",
                "{\"recipientUserId\":\"missing-user\",\"notificationType\":\"NEW_CHAT_MESSAGE\","
                    + "\"title\":\"Message\",\"body\":\"Body\",\"targetType\":\"Chat\","
                    + "\"targetId\":\"06TESTOUTBOX000000000000002\",\"metadata\":{}}",
                Instant.now()));

    for (int attempt = 1; attempt <= 5; attempt++) {
      jdbcTemplate.update(
          "UPDATE outbox_event SET next_attempt_at = CURRENT_TIMESTAMP WHERE id = ?",
          event.getId());
      worker.processDueEvents();
    }

    OutboxEvent failed = outboxRepository.findById(event.getId()).orElseThrow();
    assertThat(failed.getAttempts()).isEqualTo(5);
    assertThat(failed.getStatus()).isEqualTo(OutboxStatus.DEAD_LETTER);
    assertThat(failed.getLastError()).contains("Notification recipient not found");
  }
}
