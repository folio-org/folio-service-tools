package org.folio.spring.tools.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import feign.Headers;
import feign.Param;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("authn")
public interface AuthnClient {

  @PostMapping(value = "/login-with-expiry", consumes = APPLICATION_JSON_VALUE)
  ResponseEntity<LoginResponse> login(@RequestBody UserCredentials credentials);

  @Headers("Set-Cookie: {set_cookie}")
  @PostMapping(value = "/refresh", consumes = APPLICATION_JSON_VALUE)
  ResponseEntity<LoginResponse> refreshTokens(@Param("set_cookie") String cookieHeader);

  @PostMapping(value = "/credentials", consumes = APPLICATION_JSON_VALUE)
  void saveCredentials(@RequestBody UserCredentials credentials);

  record UserCredentials(String username, String password) {
  }
  record LoginResponse(String accessTokenExpiration, String refreshTokenExpiration) {
  }
}
