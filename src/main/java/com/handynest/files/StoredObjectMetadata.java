package com.handynest.files;

public record StoredObjectMetadata(boolean exists, long sizeBytes, String contentType) {

  public static StoredObjectMetadata missing() {
    return new StoredObjectMetadata(false, 0, null);
  }
}
