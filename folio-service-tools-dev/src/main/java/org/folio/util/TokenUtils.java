package org.folio.util;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import static org.folio.util.FutureUtils.failedFuture;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.NotAuthorizedException;

import io.vertx.core.json.JsonObject;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang.StringUtils;

import org.folio.okapi.common.XOkapiHeaders;
import org.folio.rest.tools.utils.JwtUtils;

public final class TokenUtils {

  private static final String INVALID_TOKEN_MESSAGE = "Invalid token";

  private static final String USER_ID_KEY = "user_id";
  private static final String USERNAME_KEY = "sub";

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
      String[] split = token.split("\\.");
      JsonObject j = new JsonObject(JwtUtils.getJson(split[1]));
      return Optional.of(new UserInfo(j.getString(USER_ID_KEY), j.getString(USERNAME_KEY)));
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
