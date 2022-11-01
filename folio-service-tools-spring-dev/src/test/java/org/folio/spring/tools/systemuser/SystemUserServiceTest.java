package org.folio.spring.tools.systemuser;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.tools.client.AuthnClient;
import org.folio.spring.tools.client.AuthnClient.UserCredentials;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.folio.spring.tools.model.SystemUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class SystemUserServiceTest {

  public static final String OKAPI_URL = "http://okapi";
  private static final String TENANT_ID = "test";
  @Mock
  private AuthnClient authnClient;
  @Mock
  private SystemUserExecutionContextBuilder contextBuilder;
  @Mock
  private ResponseEntity<String> expectedResponse;
  @Mock
  private FolioExecutionContext context;
  @Mock
  private FolioEnvironment environment;
  @Mock
  private Cache<String, SystemUser> userCache;

  private static SystemUser systemUserValue() {
    return SystemUser.builder().username("username").okapiUrl(OKAPI_URL).tenantId(TENANT_ID).build();
  }

  private static SystemUserProperties systemUserProperties() {
    return new SystemUserProperties("username", "password", "system", "permissions/test-permissions.csv");
  }

  @Test
  void getAuthedSystemUser_positive() {
    var expectedAuthToken = "x-okapi-token-value";
    var expectedHeaders = new HttpHeaders();
    expectedHeaders.add(XOkapiHeaders.TOKEN, expectedAuthToken);
    var systemUser = systemUserValue();

    when(authnClient.getApiKey(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    when(environment.getOkapiUrl()).thenReturn(OKAPI_URL);
    when(contextBuilder.forSystemUser(systemUser)).thenReturn(context);
    when(expectedResponse.getHeaders()).thenReturn(expectedHeaders);

    var actual = systemUserService(systemUserProperties()).getAuthedSystemUser(TENANT_ID);
    assertThat(actual.token()).isEqualTo(expectedAuthToken);
  }

  @Test
  void getAuthedSystemUserUsingCache_positive() {
    var expectedAuthToken = "x-okapi-token-value";
    var systemUserService = systemUserService(systemUserProperties());
    systemUserService.setSystemUserCache(userCache);

    when(userCache.get(eq(TENANT_ID), any())).thenReturn(systemUserValue().withToken(expectedAuthToken));

    var actual = systemUserService.getAuthedSystemUser(TENANT_ID);
    assertThat(actual.token()).isEqualTo(expectedAuthToken);
    verify(userCache).get(eq(TENANT_ID), any());
    verify(authnClient, never()).getApiKey(any());
    verify(environment, never()).getOkapiUrl();
    verify(contextBuilder, never()).forSystemUser(any());
  }

  @Test
  void authSystemUser_positive() {
    var expectedAuthToken = "x-okapi-token-value";
    var expectedHeaders = new HttpHeaders();
    expectedHeaders.add(XOkapiHeaders.TOKEN, expectedAuthToken);
    var systemUser = systemUserValue();

    when(authnClient.getApiKey(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    when(contextBuilder.forSystemUser(systemUser)).thenReturn(context);
    when(expectedResponse.getHeaders()).thenReturn(expectedHeaders);

    var actual = systemUserService(systemUserProperties()).authSystemUser(systemUser);
    assertThat(actual).isEqualTo(expectedAuthToken);
  }

  @Test
  void authSystemUser_negative_emptyHeaders() {
    when(authnClient.getApiKey(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    when(expectedResponse.getHeaders()).thenReturn(new HttpHeaders());

    var systemUser = systemUserValue();
    when(contextBuilder.forSystemUser(systemUser)).thenReturn(context);

    var systemUserService = systemUserService(systemUserProperties());
    assertThatThrownBy(() -> systemUserService.authSystemUser(systemUser))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("User [username] cannot log in");
  }

  @Test
  void authSystemUser_negative_headersDoesNotContainsRequiredValue() {
    when(authnClient.getApiKey(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    var expectedHeaders = new HttpHeaders();
    expectedHeaders.put(XOkapiHeaders.TOKEN, emptyList());
    when(expectedResponse.getHeaders()).thenReturn(expectedHeaders);

    var systemUser = systemUserValue();
    when(contextBuilder.forSystemUser(systemUser)).thenReturn(context);

    var systemUserService = systemUserService(systemUserProperties());
    assertThatThrownBy(() -> systemUserService.authSystemUser(systemUser))
      .isInstanceOf(IllegalStateException.class)
      .hasMessage("User [username] cannot log in");
  }

  private SystemUserService systemUserService(SystemUserProperties properties) {
    return new SystemUserService(contextBuilder, properties, environment, authnClient);
  }
}
