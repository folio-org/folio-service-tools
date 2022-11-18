package org.folio.spring.test.extension;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.folio.spring.test.extension.impl.DatabaseCleanupExtension;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Cleanup database if Spring application context exists
 */
@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({DatabaseCleanupExtension.class})
public @interface DatabaseCleanup {

  /**
   * Tables that should be cleaned
   */
  String[] tables();

  /**
   * Tenants that should be checked for cleanup. If empty - tables from all tenants will be cleaned.
   */
  String[] tenants() default { };
}
