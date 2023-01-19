package com.revolsys.io;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface BaseCloseable extends Closeable {
  static Consumer<BaseCloseable> CLOSER = BaseCloseable::close;

  static <C extends BaseCloseable> Consumer<? super C> closer() {
    return CLOSER;
  }

  static <R extends AutoCloseable, V> Flux<V> fluxUsing(final Callable<R> resourceSupplier,
    final Function<R, Flux<V>> action) {
    return Flux.using(resourceSupplier, action, closer());
  }

  static <R extends AutoCloseable, V> Mono<V> monoUsing(final Callable<R> resourceSupplier,
    final Function<R, Mono<V>> action) {
    return Mono.using(resourceSupplier, action, closer());
  }

  @Override
  void close();

  @SuppressWarnings("unchecked")
  default <R extends BaseCloseable, V> Flux<V> fluxUsing(final Function<R, Flux<V>> action) {
    return Flux.using(() -> ((R)this), action, CLOSER);
  }

  @SuppressWarnings("unchecked")
  default <R extends BaseCloseable, V> Mono<V> monoUsing(final Function<R, Mono<V>> action) {
    return Mono.using(() -> ((R)this), action, CLOSER);
  }

  default BaseCloseable wrap() {
    return new CloseableWrapper(this);
  }
}
