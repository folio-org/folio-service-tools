package org.folio.util;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class UserInfo {

  private String userId;
  private String userName;

  public UserInfo(String userId, String userName) {
    this.userId = userId;
    this.userName = userName;
  }

  public String getUserId() {
    return userId;
  }

  public String getUserName() {
    return userName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    UserInfo userInfo = (UserInfo) o;

    return Objects.equals(userId, userInfo.userId) &&
      Objects.equals(userName, userInfo.userName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userId, userName);
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
      .append("userId", userId)
      .append("userName", userName)
      .build();
  }
}
