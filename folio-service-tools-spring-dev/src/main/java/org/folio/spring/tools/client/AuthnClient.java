package org.folio.spring.tools.client;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("authn")
public interface AuthnClient {

  @PostMapping(value = "/login", consumes = APPLICATION_JSON_VALUE)
  ResponseEntity<String> getApiKey(@RequestBody UserCredentials credentials);

  @PostMapping(value = "/credentials", consumes = APPLICATION_JSON_VALUE)
  void saveCredentials(@RequestBody UserCredentials credentials);

  record UserCredentials(String username, String password) {
  }
}
