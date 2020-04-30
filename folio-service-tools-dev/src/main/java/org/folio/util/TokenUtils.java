package org.folio.util;

import static org.folio.util.FutureUtils.failedFuture;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import javax.ws.rs.NotAuthorizedException;

import io.vertx.core.json.JsonObject;

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

  public static CompletableFuture<UserInfo> fetchUserInfo(Map<String, String> okapiHeaders) {
    return userInfoFromToken(okapiHeaders.get(XOkapiHeaders.TOKEN))
      .map(CompletableFuture::completedFuture)
      .orElse(failedFuture(new NotAuthorizedException(INVALID_TOKEN_MESSAGE)));
  }
}
