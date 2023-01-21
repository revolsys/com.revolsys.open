package com.revolsys.io;

import java.io.Closeable;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jeometry.common.exception.Exceptions;
import org.reactivestreams.Publisher;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@FunctionalInterface
public interface BaseCloseable extends Closeable {

  static Consumer<AutoCloseable> CLOSER = (resource) -> {
    try {
      resource.close();
    } catch (final Exception e) {
      throw Exceptions.wrap(e);
    }
  };

  static <C extends BaseCloseable> Consumer<? super C> closer() {
    return CLOSER;
  }

  static <R extends AutoCloseable, V> Flux<V> fluxUsing(final Callable<R> resourceSupplier,
    final Function<R, Publisher<V>> action) {
    return Flux.using(resourceSupplier, action, CLOSER);
  }

  static <R extends AutoCloseable, V> Mono<V> monoUsing(final Callable<R> resourceSupplier,
    final Function<R, Mono<V>> action) {
    return Mono.using(resourceSupplier, action, CLOSER);
  }

  @Override
  void close();

  @SuppressWarnings("unchecked")
  default <R extends BaseCloseable, V> Flux<V> fluxUsing(final Function<R, Publisher<V>> action) {
    return BaseCloseable.fluxUsing(() -> (R)this, action);
  }

  @SuppressWarnings("unchecked")
  default <R extends BaseCloseable, V> Mono<V> monoUsing(final Function<R, Mono<V>> action) {
    return BaseCloseable.monoUsing(() -> ((R)this), action);
  }

  default BaseCloseable wrap() {
    return new CloseableWrapper(this);
  }
}
