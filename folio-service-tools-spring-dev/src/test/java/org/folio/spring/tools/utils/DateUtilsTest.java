package org.folio.spring.tools.utils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.OffsetDateTime;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

  @Test
  void toOffsetDateTime() {
    var now = Instant.now();
    var dateFromTimestamp = DateUtils.toOffsetDateTime(Timestamp.from(now));
    assertThat(dateFromTimestamp.toInstant().getEpochSecond()).isEqualTo(now.getEpochSecond());
  }

  @Test
  void toTimestamp() {
    var now = OffsetDateTime.now();
    var timestamp = DateUtils.toTimestamp(now);
    assertThat(timestamp.toInstant().getEpochSecond()).isEqualTo(now.toEpochSecond());
  }

  @Test
  void toOffsetDateTime_fromNull() {
    assertNull(DateUtils.toOffsetDateTime(null));
  }

  @Test
  void toTimestamp_fromNull() {
    assertNull(DateUtils.toTimestamp(null));
  }

  @Test
  void currentTsInString() {
    var actual = DateUtils.currentTsInString();
    assertThat(Long.valueOf(actual)).isLessThanOrEqualTo(System.currentTimeMillis());
  }

  @Test
  void currentTs() {
    var actual = DateUtils.currentTs();
    assertThat(actual.getTime()).isLessThanOrEqualTo(System.currentTimeMillis());
  }
}
