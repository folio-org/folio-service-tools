package org.folio.spring.tools.kafka;

import java.util.concurrent.Callable;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.folio.spring.FolioExecutionContext;
import org.folio.spring.scope.FolioExecutionContextSetter;
import org.folio.spring.tools.context.ExecutionContextBuilder;
import org.springframework.messaging.MessageHeaders;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaScopedExecutionService {

  private final ExecutionContextBuilder contextBuilder;

  /**
   * Executes given action in scope of kafka message.
   * It's expected that Kafka message headers contains all needed headers for building context:
   * <ul>
   *   <li>x-okapi-url</li>
   *   <li>x-okapi-tenant</li>
   * </ul>
   *
   * @param headers The tenant name.
   * @param action  Job to be executed in tenant scope.
   * @param <T>     Optional return value for the action.
   * @return Result of action.
   * @throws RuntimeException - Wrapped exception from the action.
   */
  @SneakyThrows
  public <T> T executeKafkaScoped(MessageHeaders headers, Callable<T> action) {
    try (var fex = new FolioExecutionContextSetter(folioExecutionContext(headers))) {
      return action.call();
    }
  }

  /**
   * Executes given action in scope of kafka message.
   * It's expected that Kafka message headers contains all needed headers for building context:
   * <ul>
   *   <li>x-okapi-url</li>
   *   <li>x-okapi-tenant</li>
   * </ul>
   *
   * @param headers The tenant name.
   * @param action  Job to be executed in tenant scope.
   * @throws RuntimeException - Wrapped exception from the action.
   */
  @SneakyThrows
  public void executeKafkaScoped(MessageHeaders headers, Runnable action) {
    try (var fex = new FolioExecutionContextSetter(folioExecutionContext(headers))) {
      action.run();
    }
  }

  private FolioExecutionContext folioExecutionContext(MessageHeaders headers) {
    return contextBuilder.forMessageHeaders(headers);
  }
}
