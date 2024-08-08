package org.folio.util;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import static org.folio.util.FutureUtils.failedFuture;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.NotAuthorizedException;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.folio.okapi.common.OkapiToken;
import org.folio.okapi.common.XOkapiHeaders;

public final class TokenUtils {

  private static final String INVALID_TOKEN_MESSAGE = "Invalid token";

  private TokenUtils() {
  }

  /**
   * Fetch userId and username from x-okapi-token
   *
   * @param token x-okapi-token to get info from
   * @return {@link UserInfo} that contains userId and username
   */
  public static Optional<UserInfo> userInfoFromToken(String token) {
    try {
      var okapiToken = new OkapiToken(token);
      if (okapiToken.getPayloadWithoutValidation() == null) {
        return Optional.empty();
      }
      return Optional.of(new UserInfo(
          okapiToken.getUserIdWithoutValidation(),
          okapiToken.getUsernameWithoutValidation()));
    } catch (Exception e) {
      return Optional.empty();
    }
  }

  public static CompletableFuture<UserInfo> fetchUserInfo(String token) {
    return userInfoFromToken(token)
      .map(CompletableFuture::completedFuture)
      .orElse(failedFuture(new NotAuthorizedException(INVALID_TOKEN_MESSAGE, StringUtils.defaultString(token))));
  }

  public static CompletableFuture<UserInfo> fetchUserInfo(Map<String, String> okapiHeaders) {
    Map<String, String> h = new CaseInsensitiveMap<>(defaultIfNull(okapiHeaders, Collections.emptyMap()));

    return fetchUserInfo(h.get(XOkapiHeaders.TOKEN));
  }
}
