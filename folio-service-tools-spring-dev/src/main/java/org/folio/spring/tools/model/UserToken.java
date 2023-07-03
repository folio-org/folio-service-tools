package org.folio.spring.tools.model;

import java.time.Instant;
import lombok.Builder;

@Builder
public record UserToken(String accessToken, Instant accessTokenExpiration,
                        String refreshToken, Instant refreshTokenExpiration) {
}
