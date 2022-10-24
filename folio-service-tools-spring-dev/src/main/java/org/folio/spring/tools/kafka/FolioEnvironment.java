package org.folio.spring.tools.kafka;

import static java.lang.System.getProperty;
import static java.lang.System.getenv;
import static org.apache.commons.lang3.StringUtils.firstNonBlank;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.validation.annotation.Validated;

@Data
@Validated
@Configuration
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
@ConditionalOnClass({EnableKafka.class})
@ConfigurationProperties(prefix = "application")
public class FolioEnvironment {

  @NotEmpty
  @Pattern(regexp = "[\\w\\d\\-_]+", message = "Value must follow the pattern: '[\\w0-9\\-_]+'")
  private String environment;

  /**
   * Return folio env name from environment or system properties as {@link String} object.
   *
   * @return folio env name.
   */
  public static String getFolioEnvName() {
    return firstNonBlank(getenv("ENV"), getProperty("env"), getProperty("environment"), "folio");
  }
}
