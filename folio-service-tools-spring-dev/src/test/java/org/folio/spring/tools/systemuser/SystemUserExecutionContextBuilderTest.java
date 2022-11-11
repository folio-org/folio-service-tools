package org.folio.spring.tools.systemuser;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.tools.model.SystemUser;
import org.junit.jupiter.api.Test;

class SystemUserExecutionContextBuilderTest {

  private final SystemUserExecutionContextBuilder builder =
    new SystemUserExecutionContextBuilder(mock(FolioModuleMetadata.class));

  @Test
  void canCreateSystemUserContext() {
    var systemUser = SystemUser.builder()
      .token("token").username("username")
      .okapiUrl("okapi").tenantId("tenant")
      .build();
    var context = builder.forSystemUser(systemUser);

    assertThat(context.getTenantId()).isEqualTo("tenant");
    assertThat(context.getToken()).isEqualTo("token");
    assertThat(context.getOkapiUrl()).isEqualTo("okapi");

    assertThat(context.getAllHeaders()).isNotNull();
    assertThat(context.getOkapiHeaders()).isNotNull().hasSize(3);
    assertThat(context.getFolioModuleMetadata()).isNotNull();
  }

  @Test
  void canCreateContextWithNullValues() {
    var systemUser = SystemUser.builder()
      .token(null).username("username")
      .okapiUrl(null).tenantId(null)
      .build();

    var context = builder.forSystemUser(systemUser);

    assertThat(context.getTenantId()).isNull();
    assertThat(context.getToken()).isNull();
    assertThat(context.getOkapiUrl()).isNull();

    assertThat(context.getAllHeaders()).isNotNull();
    assertThat(context.getOkapiHeaders()).isNotNull().isEmpty();
    assertThat(context.getFolioModuleMetadata()).isNotNull();
  }
}
