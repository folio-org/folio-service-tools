package org.folio.spring.tools.systemuser;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.folio.spring.tools.utils.TokenUtils.FOLIO_ACCESS_TOKEN;
import static org.folio.spring.tools.utils.TokenUtils.FOLIO_REFRESH_TOKEN;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.benmanes.caffeine.cache.Cache;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.tools.client.AuthnClient;
import org.folio.spring.tools.client.AuthnClient.UserCredentials;
import org.folio.spring.tools.client.UsersClient;
import org.folio.spring.tools.config.properties.FolioEnvironment;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.folio.spring.tools.model.SystemUser;
import org.folio.spring.tools.model.UserToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMapAdapter;

@ExtendWith(MockitoExtension.class)
class SystemUserServiceTest {

  public static final String OKAPI_URL = "http://okapi";
  private static final String TENANT_ID = "test";
  private static final Instant TOKEN_EXPIRATION = Instant.now().plus(1, ChronoUnit.DAYS);
  @Mock
  private AuthnClient authnClient;
  @Mock
  private ExecutionContextBuilder contextBuilder;
  private final ResponseEntity<AuthnClient.LoginResponse> expectedResponse = Mockito.spy(ResponseEntity.of(Optional.of(
      new AuthnClient.LoginResponse(TOKEN_EXPIRATION.toString(), TOKEN_EXPIRATION.toString()))));
  @Mock
  private FolioExecutionContext context;
  @Mock
  private FolioEnvironment environment;
  @Mock
  private PrepareSystemUserService prepareSystemUserService;
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
    var expectedUserId = UUID.randomUUID();
    var expectedUserToken = userToken(TOKEN_EXPIRATION, TOKEN_EXPIRATION);
    var expectedHeaders = cookieHeaders(expectedUserToken.accessToken(), expectedUserToken.refreshToken());

    when(authnClient.login(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    when(prepareSystemUserService.getFolioUser("username")).thenReturn(Optional.of(
      new UsersClient.User(expectedUserId.toString(), "username", true, new UsersClient.User.Personal("last"))));
    when(environment.getOkapiUrl()).thenReturn(OKAPI_URL);
    when(contextBuilder.forSystemUser(any())).thenReturn(context);
    when(expectedResponse.getHeaders()).thenReturn(expectedHeaders);

    var actual = systemUserService(systemUserProperties()).getAuthedSystemUser(TENANT_ID);
    assertThat(actual.token()).isEqualTo(expectedUserToken);
    assertThat(actual.userId()).isEqualTo(expectedUserId.toString());
  }

  @Test
  void getAuthedSystemUserUsingCache_positive() {
    var expectedUserToken = userToken(Instant.now().plus(1, ChronoUnit.DAYS), TOKEN_EXPIRATION);
    var systemUserService = systemUserService(systemUserProperties());
    systemUserService.setSystemUserCache(userCache);

    when(userCache.get(eq(TENANT_ID), any())).thenReturn(systemUserValue().withToken(expectedUserToken));

    var actual = systemUserService.getAuthedSystemUser(TENANT_ID);
    assertThat(actual.token().accessToken()).isEqualTo(expectedUserToken.accessToken());
    verify(userCache).get(eq(TENANT_ID), any());
    verify(authnClient, never()).login(any());
    verify(authnClient, never()).refreshTokens(any());
    verify(environment, never()).getOkapiUrl();
    verify(contextBuilder, never()).forSystemUser(any());
  }

  @Test
  void getAuthedSystemUserUsingCacheWithExpiredAccessToken_positive() {
    var cachedUserToken = userToken(Instant.now().minus(1, ChronoUnit.DAYS),
      Instant.now().plus(1, ChronoUnit.DAYS));
    var systemUserService = systemUserService(systemUserProperties());
    systemUserService.setSystemUserCache(userCache);

    var tokenResponseMock = cachedUserToken.accessToken() + "upd";
    var refreshResponseMock = buildClientResponse(tokenResponseMock);

    when(userCache.get(eq(TENANT_ID), any())).thenReturn(systemUserValue().withToken(cachedUserToken));
    when(authnClient.refreshTokens(any())).thenReturn(refreshResponseMock);

    var actual = systemUserService.getAuthedSystemUser(TENANT_ID);
    assertThat(actual.token().accessToken()).isEqualTo(tokenResponseMock);
    assertThat(actual.token().accessTokenExpiration()).isEqualTo(TOKEN_EXPIRATION);
    verify(authnClient).refreshTokens(startsWith(FOLIO_REFRESH_TOKEN + "=" + cachedUserToken.refreshToken()));
    verify(userCache).get(eq(TENANT_ID), any());
    verify(authnClient, never()).login(any());
  }

  @Test
  void getAuthedSystemUserUsingCacheWithExpiredRefreshToken_positive() {
    var expectedUserId = UUID.randomUUID();
    var cachedUserToken = userToken(Instant.now().minus(1, ChronoUnit.DAYS),
      Instant.now().minus(1, ChronoUnit.DAYS));
    var systemUserService = systemUserService(systemUserProperties());
    systemUserService.setSystemUserCache(userCache);

    var tokenResponseMock = cachedUserToken.accessToken() + "upd";
    var loginResponseMock = buildClientResponse(tokenResponseMock);

    when(userCache.get(eq(TENANT_ID), any())).thenReturn(systemUserValue().withToken(cachedUserToken));
    when(authnClient.login(new UserCredentials("username", "password"))).thenReturn(loginResponseMock);
    when(prepareSystemUserService.getFolioUser("username")).thenReturn(Optional.of(
      new UsersClient.User(expectedUserId.toString(), "username", true, new UsersClient.User.Personal("last"))));
    when(environment.getOkapiUrl()).thenReturn(OKAPI_URL);
    when(contextBuilder.forSystemUser(any())).thenReturn(context);

    var actual = systemUserService.getAuthedSystemUser(TENANT_ID);
    assertThat(actual.token().accessToken()).isEqualTo(tokenResponseMock);
    assertThat(actual.userId()).isEqualTo(expectedUserId.toString());
    verify(authnClient, never()).refreshTokens(any());
  }

  @Test
  void authSystemUser_positive() {
    var expectedToken = "x-okapi-token-value";
    var expectedUserToken = UserToken.builder()
      .accessToken(expectedToken)
      .accessTokenExpiration(TOKEN_EXPIRATION)
      .refreshToken(expectedToken)
      .refreshTokenExpiration(TOKEN_EXPIRATION)
      .build();
    var expectedHeaders = cookieHeaders(expectedToken);
    var systemUser = systemUserValue();

    when(authnClient.login(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    when(expectedResponse.getHeaders()).thenReturn(expectedHeaders);

    var actual = systemUserService(systemUserProperties()).authSystemUser(systemUser);
    assertThat(actual).isEqualTo(expectedUserToken);
  }

  @Test
  void authSystemUser_negative_emptyHeaders() {
    when(authnClient.login(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    when(expectedResponse.getHeaders()).thenReturn(new HttpHeaders());

    var systemUser = systemUserValue();

    var systemUserService = systemUserService(systemUserProperties());
    assertThatThrownBy(() -> systemUserService.authSystemUser(systemUser)).isInstanceOf(IllegalStateException.class)
      .hasMessage("User [username] cannot log in because of missing tokens");
  }

  @Test
  void authSystemUser_negative_headersDoesNotContainsRequiredValue() {
    when(authnClient.login(new UserCredentials("username", "password"))).thenReturn(expectedResponse);
    var expectedHeaders = new HttpHeaders();
    expectedHeaders.put(HttpHeaders.SET_COOKIE, emptyList());
    when(expectedResponse.getHeaders()).thenReturn(expectedHeaders);

    var systemUser = systemUserValue();

    var systemUserService = systemUserService(systemUserProperties());
    assertThatThrownBy(() -> systemUserService.authSystemUser(systemUser)).isInstanceOf(IllegalStateException.class)
      .hasMessage("User [username] cannot log in because of missing tokens");
  }

  @Test
  void authSystemUser_negative_emptyBody() {
    when(authnClient.login(new UserCredentials("username", "password")))
      .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    var systemUser = systemUserValue();

    var systemUserService = systemUserService(systemUserProperties());
    assertThatThrownBy(() -> systemUserService.authSystemUser(systemUser)).isInstanceOf(IllegalStateException.class)
      .hasMessage("User [username] cannot log in because expire times missing for status 200 OK");
  }

  private SystemUserService systemUserService(SystemUserProperties properties) {
    return new SystemUserService(contextBuilder, properties, environment, authnClient, prepareSystemUserService);
  }

  private UserToken userToken(Instant accessExpiration, Instant refreshExpiration) {
    return UserToken.builder()
      .accessToken("access-token")
      .accessTokenExpiration(accessExpiration)
      .refreshToken("refresh-token")
      .refreshTokenExpiration(refreshExpiration)
      .build();
  }

  private HttpHeaders cookieHeaders(String token) {
    return cookieHeaders(token, token);
  }

  private HttpHeaders cookieHeaders(String accessToken, String refreshToken) {
    return new HttpHeaders(new MultiValueMapAdapter<>(Map.of(HttpHeaders.SET_COOKIE, List.of(
      new DefaultCookie(FOLIO_ACCESS_TOKEN, accessToken).toString(),
      new DefaultCookie(FOLIO_REFRESH_TOKEN, refreshToken).toString()
    ))));
  }

  private ResponseEntity<AuthnClient.LoginResponse> buildClientResponse(String token) {
    return ResponseEntity.ok()
      .headers(cookieHeaders(token))
      .body(new AuthnClient.LoginResponse(TOKEN_EXPIRATION.toString(), TOKEN_EXPIRATION.toString()));
  }
}
