package com.handynest.catalog.category;

import com.handynest.identity.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "category_audit_event")
public class CategoryAuditEvent {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "category_id", nullable = false)
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "actor_user_id", nullable = false)
  private User actor;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private CategoryAuditAction action;

  @Column(name = "changed_fields", nullable = false, length = 2000)
  private String changedFields;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  protected CategoryAuditEvent() {}

  public CategoryAuditEvent(
      Category category,
      User actor,
      CategoryAuditAction action,
      String changedFields,
      Instant createdAt) {
    this.category = category;
    this.actor = actor;
    this.action = action;
    this.changedFields = changedFields;
    this.createdAt = createdAt;
  }

  public Category getCategory() {
    return category;
  }

  public User getActor() {
    return actor;
  }

  public CategoryAuditAction getAction() {
    return action;
  }

  public String getChangedFields() {
    return changedFields;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}
