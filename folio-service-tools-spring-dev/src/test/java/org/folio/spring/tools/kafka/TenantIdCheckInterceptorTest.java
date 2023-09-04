package org.folio.spring.tools.kafka;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.MessageFormatter;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.folio.spring.tools.kafka.FolioKafkaProperties.TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TenantIdCheckInterceptorTest {

  private static TestAppender appender;

  @BeforeAll
  public static void classSetup() {
    Logger logger = LogManager.getLogger(TenantIdCheckInterceptor.class.getName());
    appender = new TestAppender("TestAppender", null);
    ((LoggerContext) LogManager.getContext(false)).getConfiguration().addAppender(appender);
    ((org.apache.logging.log4j.core.Logger) logger).addAppender(appender);
    appender.start();
  }

  @Test
  public void onSend() {
    String topicName = "topicName";
    String key = "key-0";
    String value = "value-0";
    //
    // create a record with no headers, message should be logged
    ProducerRecord<String, String> kafkaRecord = new ProducerRecord<>(topicName, 0, key, value);
    TenantIdCheckInterceptor tenantIdCheckInterceptor = new TenantIdCheckInterceptor();

    tenantIdCheckInterceptor.onSend(kafkaRecord);

    assertEquals(1, appender.getMessages().size());
    assertEquals(MessageFormatter.format(TenantIdCheckInterceptor.TENANT_ID_ERROR_MESSAGE, topicName).getMessage()
      , appender.getMessages().get(0));

    // clear logged messages
    appender.clear();

    // create record with necessary headers, message should not be logged
    kafkaRecord = new ProducerRecord<>(topicName, 0, key, value);
    kafkaRecord.headers().add(TENANT_ID, value.getBytes(StandardCharsets.UTF_8));

    tenantIdCheckInterceptor.onSend(kafkaRecord);

    assertEquals(0, appender.getMessages().size());
  }

  private static class TestAppender extends AbstractAppender {

    private final List<String> messages = new ArrayList<>();

    TestAppender(String name, org.apache.logging.log4j.core.Filter filter) {
      super(name, filter, null);
    }

    @Override
    public void append(LogEvent event) {
      messages.add(event.getMessage().getFormattedMessage());
    }

    List<String> getMessages() {
      return messages;
    }

    void clear() {
      messages.clear();
    }
  }

}
