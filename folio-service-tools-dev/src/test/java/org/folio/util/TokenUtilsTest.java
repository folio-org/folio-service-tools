package org.folio.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.folio.util.FutureUtils.mapCompletableFuture;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import jakarta.ws.rs.NotAuthorizedException;

import io.vertx.core.Future;
import org.folio.test.extensions.TestStartLoggingExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import org.folio.okapi.common.XOkapiHeaders;

class TokenUtilsTest {

  @RegisterExtension
  TestStartLoggingExtension startLoggingExtension = TestStartLoggingExtension.instance();

  private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiJ9."
    + "eyJzdWIiOiJURVNUX1VTRVJfTkFNRSIsInVzZXJfaWQiOiJURVNUX1VTRVJfSUQiLCJpYXQiOjE1ODU4OTUxNDQsInRlbmFudCI6ImRpa3UifQ."
    + "xoJ_lJqjGmDUdIoHDdTdPtssQnV_xjN7I8QPBsbrxi4";

  private static final String USER_ID = "TEST_USER_ID";
  private static final String USER_NAME = "TEST_USER_NAME";

  private static final String INVALID_TOKEN = "invalidToken";


  @Test
  void testValidToken() {
    Optional<UserInfo> actual = TokenUtils.userInfoFromToken(VALID_TOKEN);

    assertTrue(actual.isPresent());
    assertEquals(USER_ID, actual.get().getUserId());
    assertEquals(USER_NAME, actual.get().getUserName());
  }

  @Test
  void testInvalidToken() {
    Optional<UserInfo> actual = TokenUtils.userInfoFromToken(INVALID_TOKEN);

    assertFalse(actual.isPresent());
  }

  @Test
  void testFetchReturnsUserInfoFromValidToken() throws ExecutionException, InterruptedException {
    Map<String, String> headers = new HashMap<>();
    headers.put(XOkapiHeaders.TOKEN, VALID_TOKEN);

    CompletableFuture<UserInfo> future = TokenUtils.fetchUserInfo(headers);

    UserInfo result = future.get();

    assertEquals(USER_ID, result.getUserId());
    assertEquals(USER_NAME, result.getUserName());
  }

  @Test
  void testFetchIsCaseInsensitiveToHeaderNames() throws ExecutionException, InterruptedException {
    Map<String, String> headers = new HashMap<>();
    headers.put(XOkapiHeaders.TOKEN.toUpperCase(), VALID_TOKEN);

    CompletableFuture<UserInfo> future = TokenUtils.fetchUserInfo(headers);

    UserInfo result = future.get();

    assertEquals(USER_ID, result.getUserId());
    assertEquals(USER_NAME, result.getUserName());
  }

  @Test
  void testFetchFailedWithNotAuthorizedWhenEmptyToken() {
    Future<UserInfo> result = mapCompletableFuture(TokenUtils.fetchUserInfo(Collections.emptyMap()));

    assertTrue(result.failed());

    Throwable cause = result.cause().getCause();
    assertThat(cause, instanceOf(NotAuthorizedException.class));
    assertThat(((NotAuthorizedException) cause).getChallenges(), contains(""));
  }

  @Test
  void testFetchFailedWithNotAuthorizedWhenInvalidToken() {
    Map<String, String> headers = new HashMap<>();
    headers.put(XOkapiHeaders.TOKEN, INVALID_TOKEN);

    Future<UserInfo> result = mapCompletableFuture(TokenUtils.fetchUserInfo(headers));

    assertTrue(result.failed());

    Throwable cause = result.cause().getCause();
    assertThat(cause, instanceOf(NotAuthorizedException.class));
    assertThat(((NotAuthorizedException) cause).getChallenges(), contains(INVALID_TOKEN));
  }
}
