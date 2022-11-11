package org.folio.spring.tools.systemuser;

import static java.util.Collections.singleton;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

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
    var okapiUrl = systemUser.okapiUrl();
    var tenantId = systemUser.tenantId();
    var token = systemUser.token();

    Map<String, Collection<String>> headers = new HashMap<>();
    if (isNotBlank(okapiUrl)) {
      headers.put(XOkapiHeaders.URL, singleton(okapiUrl));
    }
    if (isNotBlank(tenantId)) {
      headers.put(XOkapiHeaders.TENANT, singleton(tenantId));
    }
    if (isNotBlank(token)) {
      headers.put(XOkapiHeaders.TOKEN, singleton(token));
    }

    return builder()
      .withTenantId(tenantId)
      .withOkapiUrl(okapiUrl)
      .withToken(token)
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
