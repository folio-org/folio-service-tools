package org.folio.common.pf;

@FunctionalInterface
public interface LogHandler<T> {

  void log(T t);

}
