package org.folio.spring.tools.systemuser;

import com.github.benmanes.caffeine.cache.Cache;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.tools.client.AuthnClient;
import org.folio.spring.tools.client.AuthnClient.UserCredentials;
import org.folio.spring.tools.client.UsersClient;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.folio.spring.tools.model.SystemUser;
import org.springframework.beans.factory.annotation.Autowired;
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
   * Authenticate system user.
   *
   * @param tenantId The tenant name
   * @return {@link org.folio.spring.tools.model.SystemUser} with token value
   */
  public SystemUser getAuthedSystemUser(String tenantId) {
    if (systemUserCache == null) {
      return getSystemUser(tenantId);
    } else {
      return systemUserCache.get(tenantId, this::getSystemUser);
    }
  }

  /**
   * Authenticate system user and return token value.
   *
   * @param user {@link org.folio.spring.tools.model.SystemUser} to log with
   * @return token value
   */
  public String authSystemUser(SystemUser user) {
    var response = authnClient.getApiKey(new UserCredentials(user.username(), systemUserProperties.password()));

    return Optional.ofNullable(response.getHeaders().get(XOkapiHeaders.TOKEN))
      .filter(list -> !CollectionUtils.isEmpty(list))
      .map(list -> list.get(0))
      .orElseThrow(() -> new IllegalStateException(String.format("User [%s] cannot log in", user.username())));
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

}
