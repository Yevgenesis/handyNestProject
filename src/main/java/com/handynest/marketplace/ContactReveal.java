package com.handynest.marketplace;

import com.handynest.common.domain.PublicIdEntity;
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
import lombok.Getter;

@Getter
@Entity
@Table(name = "contact_reveal")
public class ContactReveal extends PublicIdEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "deal_id", nullable = false)
  private Deal deal;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "requested_by_user_id", nullable = false)
  private User requestedBy;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "subject_user_id", nullable = false)
  private User subjectUser;

  @Enumerated(EnumType.STRING)
  @Column(name = "contact_type", nullable = false, length = 20)
  private ContactType contactType;

  @Column(name = "ip_address", nullable = false, length = 64)
  private String ipAddress;

  @Column(name = "user_agent", length = 500)
  private String userAgent;

  protected ContactReveal() {}

  public ContactReveal(
      Deal deal,
      User requestedBy,
      User subjectUser,
      ContactType contactType,
      String ipAddress,
      String userAgent) {
    this.deal = deal;
    this.requestedBy = requestedBy;
    this.subjectUser = subjectUser;
    this.contactType = contactType;
    this.ipAddress = ipAddress;
    this.userAgent = userAgent;
  }
}
