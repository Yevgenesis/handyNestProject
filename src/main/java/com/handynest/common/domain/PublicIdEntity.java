package com.handynest.common.domain;

import com.handynest.common.publicid.PublicIdGenerator;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;

@MappedSuperclass
public abstract class PublicIdEntity extends BaseAuditEntity {

  @Column(name = "public_id", nullable = false, unique = true, updatable = false, length = 26)
  private String publicId;

  public String getPublicId() {
    return publicId;
  }

  protected void setPublicId(String publicId) {
    this.publicId = publicId;
  }

  @PrePersist
  protected void ensurePublicId() {
    if (publicId == null || publicId.isBlank()) {
      publicId = PublicIdGenerator.defaultGenerator().newUlid();
    }
  }
}
