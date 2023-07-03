package org.folio.spring.tools.context;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import org.folio.spring.FolioModuleMetadata;
import org.folio.spring.integration.XOkapiHeaders;
import org.folio.spring.tools.model.SystemUser;
import org.folio.spring.tools.model.UserToken;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.MessageHeaders;

class ExecutionContextBuilderTest {

  private final ExecutionContextBuilder builder =
    new ExecutionContextBuilder(mock(FolioModuleMetadata.class));

  @Test
  void canCreateSystemUserContextForSystemUser() {
    var userId = UUID.randomUUID();
    var systemUser = SystemUser.builder()
      .token(new UserToken("token", Instant.EPOCH, "", Instant.EPOCH)).username("username")
      .okapiUrl("okapi").tenantId("tenant")
      .userId(userId.toString())
      .build();
    var context = builder.forSystemUser(systemUser);

    assertThat(context.getTenantId()).isEqualTo("tenant");
    assertThat(context.getToken()).isEqualTo("token");
    assertThat(context.getOkapiUrl()).isEqualTo("okapi");
    assertThat(context.getUserId()).isEqualTo(userId);

    assertThat(context.getAllHeaders()).isNotNull();
    assertThat(context.getOkapiHeaders()).isNotNull().hasSize(4);
    assertThat(context.getFolioModuleMetadata()).isNotNull();
  }

  @Test
  void canCreateSystemUserContextForMessageHeaders() {
    var userId = UUID.randomUUID();
    var messageHeaders = new MessageHeaders(Map.of(
      XOkapiHeaders.TOKEN, "token".getBytes(),
      XOkapiHeaders.TENANT, "tenant".getBytes(),
      XOkapiHeaders.URL, "okapi".getBytes(),
      XOkapiHeaders.USER_ID, userId.toString().getBytes(),
      XOkapiHeaders.REQUEST_ID, "request".getBytes()));
    var context = builder.forMessageHeaders(messageHeaders);

    assertThat(context.getTenantId()).isEqualTo("tenant");
    assertThat(context.getToken()).isEqualTo("token");
    assertThat(context.getOkapiUrl()).isEqualTo("okapi");
    assertThat(context.getRequestId()).isEqualTo("request");
    assertThat(context.getUserId()).isEqualTo(userId);

    assertThat(context.getAllHeaders()).isNotNull();
    assertThat(context.getOkapiHeaders()).isNotNull().hasSize(5);
    assertThat(context.getFolioModuleMetadata()).isNotNull();
  }

  @Test
  void canCreateContextWithNullValues() {
    var systemUser = SystemUser.builder()
      .token(null).username("username")
      .okapiUrl(null).tenantId(null)
      .userId(null)
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
