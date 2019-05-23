package org.folio.common.log;

@FunctionalInterface
public interface LogHandler<T> {

  void log(T t);

}
