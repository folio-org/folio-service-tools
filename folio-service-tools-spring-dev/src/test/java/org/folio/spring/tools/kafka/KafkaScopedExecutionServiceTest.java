package org.folio.spring.tools.kafka;

import static java.util.Collections.emptyMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.Callable;
import org.folio.spring.DefaultFolioExecutionContext;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.MessageHeaders;

@ExtendWith(MockitoExtension.class)
class KafkaScopedExecutionServiceTest {

  @InjectMocks
  private KafkaScopedExecutionService kafkaScopedExecutionService;
  @Mock
  private ExecutionContextBuilder contextBuilder;

  @Test
  void executeKafkaScoped_positive_callable() {
    var messageHeaders = new MessageHeaders(Map.of("header1", "val"));
    when(contextBuilder.forMessageHeaders(messageHeaders)).thenReturn(
      new DefaultFolioExecutionContext(null, emptyMap()));

    var actual = kafkaScopedExecutionService.executeKafkaScoped(messageHeaders, () -> "result");

    assertThat(actual).isEqualTo("result");
  }

  @Test
  void executeKafkaScoped_positive_runnable() {
    var messageHeaders = new MessageHeaders(Map.of("header1", "val"));
    when(contextBuilder.forMessageHeaders(messageHeaders)).thenReturn(
      new DefaultFolioExecutionContext(null, emptyMap()));
    var runnableMock = mock(Runnable.class);

    kafkaScopedExecutionService.executeKafkaScoped(messageHeaders, runnableMock);

    verify(runnableMock).run();
  }

  @Test
  void executeSystemUserScoped_negative_throwsException() {
    var messageHeaders = new MessageHeaders(Map.of("header1", "val"));
    when(contextBuilder.forMessageHeaders(messageHeaders)).thenReturn(
      new DefaultFolioExecutionContext(null, emptyMap()));

    Callable<Object> callable = () -> {
      throw new Exception("error");
    };

    assertThatThrownBy(() -> kafkaScopedExecutionService.executeKafkaScoped(messageHeaders, callable))
      .isInstanceOf(Exception.class)
      .hasMessage("error");
  }

}
