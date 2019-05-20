package org.folio.test.junit.vertx;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class MVertxUnitRunner extends VertxUnitRunner {

  public MVertxUnitRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override
  protected void invokeTestMethod(FrameworkMethod fMethod, Object test, TestContext context)
      throws InvocationTargetException {
    Method method = fMethod.getMethod();

    Class<?>[] paramTypes = method.getParameterTypes();
    try {
      if (paramTypes.length == 0) {
        fMethod.invokeExplosively(test);
      } else {
        fMethod.invokeExplosively(test, context);
      }
    } catch (Throwable t) {
      throw new InvocationTargetException(t);
    }
  }
}
