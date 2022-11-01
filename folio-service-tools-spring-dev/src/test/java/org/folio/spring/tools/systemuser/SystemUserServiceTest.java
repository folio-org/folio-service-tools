package org.folio.spring.tools.systemuser;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

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

  private static SystemUser systemUserValue() {
    return SystemUser.builder().username("username").okapiUrl("http://okapi").tenantId(TENANT_ID).build();
  }

  private static SystemUserProperties systemUserProperties() {
    return new SystemUserProperties("username", "password", "system", "permissions/test-permissions.csv");
  }

  @Test
  void loginSystemUser_positive() {
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
  void loginSystemUser_negative_emptyHeaders() {
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
  void loginSystemUser_negative_headersDoesNotContainsRequiredValue() {
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
