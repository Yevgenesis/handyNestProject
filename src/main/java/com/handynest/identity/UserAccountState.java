package com.handynest.identity;

public final class UserAccountState {

  private UserAccountState() {}

  public static boolean isSessionAllowed(User user) {
    return user != null
        && !user.isDeleted()
        && user.getStatus() != UserStatus.DELETED
        && user.getStatus() != UserStatus.BLOCKED;
  }
}
