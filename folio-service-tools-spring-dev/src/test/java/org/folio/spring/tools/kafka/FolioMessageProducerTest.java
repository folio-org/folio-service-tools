package org.folio.spring.tools.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.folio.spring.FolioExecutionContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

@ExtendWith(MockitoExtension.class)
class FolioMessageProducerTest {

  private static final String TENANT_ID = "test-tenant";

  @Mock
  private KafkaTemplate<String, TestMessage> template;
  @Spy
  private TestTopic testTopic;
  @Mock
  private FolioExecutionContext context;
  @Captor
  private ArgumentCaptor<ProducerRecord<String, TestMessage>> producerRecordCaptor;

  @InjectMocks
  private FolioMessageProducer<TestMessage> producer;

  @Test
  void sendMessages() {
    var testMessage = new TestMessage("test");

    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(context.getOkapiHeaders()).thenReturn(Map.of());
    when(template.send(producerRecordCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));

    producer.setContext(context);
    producer.sendMessages(List.of(testMessage));

    var actual = producerRecordCaptor.getValue();

    assertMsgBodyIsValid(actual);

    assertThat(actual.headers()).isEmpty();
  }

  @Test
  void sendMessages_withHeaders() {
    var testMessage = new TestMessage("test");

    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(context.getOkapiHeaders()).thenReturn(Map.of(
      "okapi-header", List.of("okapi"),
      "header1", List.of("val1"),
      "header2", List.of("val2")
    ));
    when(template.send(producerRecordCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));

    producer.setContext(context);
    producer.sendMessages(List.of(testMessage));

    var actual = producerRecordCaptor.getValue();

    assertMsgBodyIsValid(actual);
    assertThat(actual.headers())
      .extracting(Header::key, header -> new String(header.value()))
      .contains(tuple("header2", "val2"), tuple("header1", "val1"), tuple("okapi-header", "okapi"));

  }

  @Test
  void sendMessages_withHeadersFiltering() {
    var testMessage = new TestMessage("test");

    when(context.getTenantId()).thenReturn(TENANT_ID);
    when(context.getAllHeaders()).thenReturn(Map.of(
      "okapi-header", List.of("okapi"),
      "header1", List.of("val1"),
      "header2", List.of("val2")
    ));
    when(template.send(producerRecordCaptor.capture())).thenReturn(CompletableFuture.completedFuture(null));

    producer.setContext(context);
    producer.setHeaderPredicate(header -> header.key().startsWith("okapi"));
    producer.sendMessages(List.of(testMessage));

    var actual = producerRecordCaptor.getValue();

    assertMsgBodyIsValid(actual);
    assertThat(actual.headers())
      .extracting(Header::key, header -> new String(header.value()))
      .contains(tuple("okapi-header", "okapi"));

  }

  private void assertMsgBodyIsValid(ProducerRecord<String, TestMessage> actual) {
    assertThat(actual.topic()).isEqualTo("folio.test-tenant.test");
    assertThat(actual.value()).extracting("value", "tenant")
      .containsExactly("test", TENANT_ID);
    assertThat(Long.valueOf(actual.value().getTs())).isLessThanOrEqualTo(System.currentTimeMillis());
  }

  @Getter
  @Setter
  @RequiredArgsConstructor
  static class TestMessage implements BaseKafkaMessage {

    private final String value;
    private String tenant;
    private String ts;
  }

  static class TestTopic implements FolioKafkaTopic {

    @Override
    public String topicName() {
      return "test";
    }

    @Override
    public String envId() {
      return "folio";
    }
  }
}
