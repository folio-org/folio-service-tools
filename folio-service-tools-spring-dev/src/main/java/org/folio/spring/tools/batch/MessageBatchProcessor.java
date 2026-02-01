package org.folio.spring.tools.batch;

import static java.util.Collections.singletonList;
import static org.folio.spring.tools.config.RetryTemplateConfiguration.DEFAULT_RETRY_TEMPLATE_NAME;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@RequiredArgsConstructor
public class MessageBatchProcessor {

  private final Map<String, RetryTemplate> retryTemplateBeans;
  @Qualifier(value = DEFAULT_RETRY_TEMPLATE_NAME)
  private final RetryTemplate defaultRetryTemplate;

  /**
   * Consumes batch of values as list and tries to process them using the strategy with retry.
   *
   * <p> At first, a batch will be retried by the specified retry policy, then, if it's failing, it would be processed
   * by single value at one time, if the value would be failed to process - failedValueConsumer will be executed.
   * </p>
   *
   * @param batch               list of values as {@link java.util.List} object
   * @param retryBeanName       retry bean name, if it's not specified - default retry policy will be used.
   * @param batchConsumer       batch consumer as {@link java.util.function.Consumer} lambda function
   * @param failedValueConsumer bi value consumer, where first - is the failed value, second is the related error
   * @param <T>                 generic type for batch value
   */
  public <T> void consumeBatchWithFallback(List<T> batch, String retryBeanName,
                                           Consumer<List<T>> batchConsumer,
                                           BiConsumer<T, Throwable> failedValueConsumer) {
    var beanName = retryBeanName == null ? "" : retryBeanName;
    var retryTemplate = retryTemplateBeans.getOrDefault(beanName, defaultRetryTemplate);
    if (batch == null || batch.isEmpty()) {
      log.info("Resources batch is empty, skipping it.");
      return;
    }

    try {
      executeWithRetryTemplate(retryTemplate, batch, batchConsumer);
    } catch (RetryException e) {
      if (batch.size() == 1) {
        failedValueConsumer.accept(batch.getFirst(), e.getLastException());
      } else {
        log.warn("Failed to process batch, attempting to process resources one by one", e);
        processMessagesOneByOne(batch, retryTemplate, batchConsumer, failedValueConsumer);
      }
    }
  }

  private <T> void processMessagesOneByOne(List<T> batch, RetryTemplate retryTemplate,
                                           Consumer<List<T>> batchConsumer,
                                           BiConsumer<T, Throwable> failedValueConsumer) {
    for (T batchValue : batch) {
      try {
        executeWithRetryTemplate(retryTemplate, singletonList(batchValue), batchConsumer);
      } catch (RetryException e) {
        failedValueConsumer.accept(batchValue, e.getLastException());
      }
    }
  }

  private <T> void executeWithRetryTemplate(RetryTemplate retryTemplate, List<T> batch, Consumer<List<T>> consumer)
    throws RetryException {
    retryTemplate.execute(() -> {
      consumer.accept(batch);
      return null;
    });
  }
}
