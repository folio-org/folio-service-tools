package org.folio.spring.tools.systemuser;

import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.tools.model.SystemUser;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemUserExecutionContextBuilder {

  private final FolioModuleMetadata moduleMetadata;

  public Builder builder() {
    return new Builder(moduleMetadata);
  }

  public FolioExecutionContext forSystemUser(SystemUser systemUser) {
    Map<String, Collection<String>> headers = Map.of(
      XOkapiHeaders.URL, singleton(systemUser.okapiUrl()),
      XOkapiHeaders.TENANT, singleton(systemUser.tenantId()),
      XOkapiHeaders.TOKEN, singleton(systemUser.token())
    );
    return builder()
      .withTenantId(systemUser.tenantId())
      .withOkapiUrl(systemUser.okapiUrl())
      .withToken(systemUser.token())
      .withOkapiHeaders(headers)
      .build();
  }

  @With
  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final FolioModuleMetadata moduleMetadata;
    private final Map<String, Collection<String>> allHeaders;
    private final Map<String, Collection<String>> okapiHeaders;
    private String tenantId;
    private String okapiUrl;
    private String token;

    public Builder(FolioModuleMetadata moduleMetadata) {
      this.moduleMetadata = moduleMetadata;
      this.allHeaders = new HashMap<>();
      this.okapiHeaders = new HashMap<>();
    }

    public FolioExecutionContext build() {
      return new FolioExecutionContext() {
        @Override
        public String getTenantId() {
          return tenantId;
        }

        @Override
        public String getOkapiUrl() {
          return okapiUrl;
        }

        @Override
        public String getToken() {
          return token;
        }

        @Override
        public Map<String, Collection<String>> getAllHeaders() {
          return allHeaders;
        }

        @Override
        public Map<String, Collection<String>> getOkapiHeaders() {
          return okapiHeaders;
        }

        @Override
        public FolioModuleMetadata getFolioModuleMetadata() {
          return moduleMetadata;
        }
      };
    }
  }
}
