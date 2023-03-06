package org.folio.spring.tools.kafka;

public interface BaseKafkaMessage {

  void setTenant(String tenant);

  String getTenant();

  void setTs(String ts);

  String getTs();
}
