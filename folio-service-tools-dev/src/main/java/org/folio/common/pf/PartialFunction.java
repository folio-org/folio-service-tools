package org.folio.common.pf;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

/**
 * Inspired by <a href="https://www.scala-lang.org/api/current/scala/PartialFunction.html">Scala implementation</a>
 * of partial functions.
 *
 * @param <T> the type of the input to the function
 * @param <R> the type of the result of the function
 */
public interface PartialFunction<T, R> extends Function<T, R> {

  @Override
  default R apply(T t) {
    return applyOrElse(t, PartialFunctions.empty());
  }

  /**
   * Applies this partial function to the given argument when it is contained in the function domain.
   * Applies fallback function where this partial function is not defined.
   *
   * @param t the function argument
   * @param otherwise the fallback function
   * @return the result of this function or fallback function application.
   */
  default R applyOrElse(T t, Function<? super T, ? extends R> otherwise) {
    Objects.requireNonNull(otherwise);
    return isDefinedAt(t) ? applySuccessfully(t) : otherwise.apply(t);
  }

  boolean isDefinedAt(T t);

  /**
   * Important:
   * The method shouldn't be called directly. It is more of internal one.
   * Use {@link #apply(Object)} or {@link #applyOrElse(Object, Function)}
   */
  R applySuccessfully(T t);

  @Override
  default <V> PartialFunction<V, R> compose(Function<? super V, ? extends T> before) {
    Objects.requireNonNull(before);
    return PartialFunctions.compose(this, before);
  }
  /**
   * Composes this partial function with a fallback partial function which
   * gets applied where this partial function is not defined.
   *
   * @param fallback  the fallback function
   * @return a partial function which has as domain the union of the domains
   *         of this partial function and <code>fallback</code>. The resulting partial function
   *         takes {@code x} to {@code this(x)} where {@code this}, and to
   *         {@code fallback(x)} where it is not.
   */
  default PartialFunction<T, R> orElse(PartialFunction<T, R> fallback) {
    Objects.requireNonNull(fallback);
    return PartialFunctions.orElse(this, fallback);
  }

  /**
   * Composes this partial function with a transformation function that
   * gets applied to results of this partial function.
   *
   * @param after the transformation function
   * @param <V>   the result type of the transformation function.
   * @return a partial function with the same domain as this partial function, which maps
   *         arguments {@code x} to {@code after(this(x))}.
   */
  @Override
  default <V> PartialFunction<T, V> andThen(Function<? super R, ? extends V> after) {
    Objects.requireNonNull(after);
    return PartialFunctions.andThen(this, after);
  }

  /**
   * Turns this partial function into a plain function returning an {@code Optional} result.
   *
   * @return a function that takes an argument {@code x} to {@code Optional.of(this(x))} if {@code this}
   *         is defined for {@code x}, and to {@code Option.empty()} otherwise.
   */
  default Function<T, Optional<R>> lift() {
    return PartialFunctions.lift(this);
  }
}
