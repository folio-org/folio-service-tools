package org.folio.spring.tools.systemuser;

import static java.util.Objects.isNull;
import static org.folio.spring.tools.utils.TokenUtils.encodeRefreshTokenToCookie;
import static org.folio.spring.tools.utils.TokenUtils.parseUserTokenFromCookies;
import static org.springframework.http.HttpHeaders.SET_COOKIE;

import com.github.benmanes.caffeine.cache.Cache;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.tools.client.AuthnClient;
import org.folio.spring.tools.client.AuthnClient.UserCredentials;
import org.folio.spring.tools.client.UsersClient;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.folio.spring.tools.model.SystemUser;
import org.folio.spring.tools.model.UserToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Log4j2
@Service
@RequiredArgsConstructor
public class SystemUserService {

  private final ExecutionContextBuilder contextBuilder;
  private final SystemUserProperties systemUserProperties;
  private final FolioEnvironment environment;
  private final AuthnClient authnClient;
  private final PrepareSystemUserService prepareUserService;

  private Cache<String, SystemUser> systemUserCache;

  /**
   * Get authenticate system user.
   * Get from cache if present (or login otherwise). Call refresh token endpoint in case
   * access token expired. Call login endpoint in case refresh token expired
   *
   * @param tenantId The tenant name
   * @return {@link org.folio.spring.tools.model.SystemUser} with token value
   */
  public SystemUser getAuthedSystemUser(String tenantId) {
    if (systemUserCache == null) {
      return getSystemUser(tenantId);
    }

    var user = systemUserCache.get(tenantId, this::getSystemUser);
    var userToken = user.token();
    var now = Instant.now();
    if (userToken.accessTokenExpiration().isAfter(now)) {
      return user;
    }

    systemUserCache.invalidate(tenantId);
    if (userToken.refreshTokenExpiration().isAfter(now)) {
      var newToken = refreshUserToken(user.username(), userToken);
      user = user.withToken(newToken);
    } else {
      user = getSystemUser(tenantId);
    }
    systemUserCache.put(tenantId, user);

    return user;
  }

  /**
   * Authenticate system user and return token value.
   *
   * @param user {@link org.folio.spring.tools.model.SystemUser} to log with
   * @return token value
   */
  public UserToken authSystemUser(SystemUser user) {
    return getToken(() -> authnClient.login(new UserCredentials(user.username(), systemUserProperties.password())),
      user.username(), "log in");
  }

  @Autowired(required = false)
  public void setSystemUserCache(Cache<String, SystemUser> systemUserCache) {
    this.systemUserCache = systemUserCache;
  }

  private SystemUser getSystemUser(String tenantId) {
    log.info("Attempting to issue token for system user [tenantId={}]", tenantId);
    var systemUser = SystemUser.builder()
      .tenantId(tenantId)
      .username(systemUserProperties.username())
      .okapiUrl(environment.getOkapiUrl())
      .build();

    // create context for authentication
    try (var fex = new FolioExecutionContextSetter(contextBuilder.forSystemUser(systemUser))) {
      var token = authSystemUser(systemUser);
      systemUser = systemUser.withToken(token);
      log.info("Token for system user has been issued [tenantId={}]", tenantId);
    }
    // create context for user with token for getting user id
    try (var fex = new FolioExecutionContextSetter(contextBuilder.forSystemUser(systemUser))) {
      var userId = prepareUserService.getFolioUser(systemUserProperties.username())
        .map(UsersClient.User::id).orElse(null);
      return systemUser.withUserId(userId);
    }
  }

  private UserToken refreshUserToken(String username, UserToken oldToken) {
    var refreshTokenCookie = encodeRefreshTokenToCookie(oldToken.refreshToken(), oldToken.refreshTokenExpiration());
    return getToken(() -> authnClient.refreshTokens(refreshTokenCookie), username, "refresh");
  }

  private UserToken getToken(Supplier<ResponseEntity<AuthnClient.LoginResponse>> tokenSupplier,
                             String username, String action) {
    var response = tokenSupplier.get();

    if (isNull(response.getBody())) {
      throw new IllegalStateException(String.format(
        "User [%s] cannot %s because expire times missing for status %s", username, action, response.getStatusCode()));
    }

    return Optional.ofNullable(response.getHeaders().get(SET_COOKIE))
      .filter(list -> !CollectionUtils.isEmpty(list))
      .map(cookieHeaders -> parseUserTokenFromCookies(cookieHeaders, response.getBody()))
      .orElseThrow(() -> new IllegalStateException(String.format(
        "User [%s] cannot %s because of missing tokens", username, action)));
  }

}
