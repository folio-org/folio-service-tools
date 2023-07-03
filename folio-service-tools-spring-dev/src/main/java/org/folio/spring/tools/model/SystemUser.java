package org.folio.spring.tools.model;

import lombok.Builder;
import lombok.With;

@Builder
public record SystemUser(String username, String okapiUrl, String tenantId,
                         @With UserToken token, @With String userId) {
}
