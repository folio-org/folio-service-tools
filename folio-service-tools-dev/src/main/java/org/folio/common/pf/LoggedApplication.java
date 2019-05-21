package org.folio.common.pf;

import java.util.Objects;

import org.folio.common.log.LogHandler;

final class LoggedApplication<T, R> implements PartialFunction<T, R> {

  private PartialFunction<T, R> pf;
  private LogHandler<? super T> logHandler;


  LoggedApplication(PartialFunction<T, R> pf, LogHandler<? super T> logHandler) {
    Objects.requireNonNull(pf, "Partial function is null");
    Objects.requireNonNull(logHandler, "Log handler is null");

    this.pf = pf;
    this.logHandler = logHandler;
  }

  @Override
  public R apply(T t) {
    logHandler.log(t);
    return pf.apply(t);
  }

  @Override
  public boolean isDefinedAt(T t) {
    return pf.isDefinedAt(t);
  }

  @Override
  public R applySuccessfully(T t) {
    return pf.applySuccessfully(t);
  }

}
