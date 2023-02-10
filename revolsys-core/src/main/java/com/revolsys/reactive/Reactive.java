package com.revolsys.reactive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jeometry.common.exception.Exceptions;
import org.reactivestreams.Publisher;

import com.revolsys.collection.list.Lists;
import com.revolsys.io.BaseCloseable;
import com.revolsys.io.file.Paths;
import com.revolsys.util.Pair;

import reactor.core.Disposable;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;

public class Reactive {

  private static class LatchDisposable implements Disposable {
    private final CountDownLatch latch;

    private final Disposable disposable;

    public LatchDisposable(final CountDownLatch latch, final Disposable disposable) {
      this.latch = latch;
      this.disposable = disposable;
    }

    @Override
    public void dispose() {
      this.latch.countDown();
      this.disposable.dispose();
    }

    @Override
    public boolean isDisposed() {
      return this.latch.getCount() <= 0 || this.disposable.isDisposed();
    }

  }

  private static final Consumer<Disposable> NOOPCALLBACK = d -> {
  };

  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <V> Mono<V> combineLatestFirst(final Publisher<V> resultSource,
    final Publisher<? extends Object>... sources) {
    final List<Publisher<? extends Object>> publishers = Lists.newArray(resultSource);
    Lists.addAll(publishers, sources);
    return Flux.combineLatest(publishers, results -> (V)results[0]).single();
  }

  @SuppressWarnings("unchecked")
  @SafeVarargs
  public static <V> Mono<V> combineLatestIndex(final int index,
    final Publisher<? extends Object>... sources) {
    return Flux.combineLatest(results -> (V)results[index], sources).single();
  }

  public static <T> Flux<T> debugTime(final String message, final Flux<T> flux) {
    final AtomicReference<Long> startTime = new AtomicReference<>();
    return flux.doOnSubscribe(x -> startTime.set(System.nanoTime()))
      .doFinally(x -> System.out.println("message : "
        + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime.get()) + " ms."));
  }

  public static <T> Mono<T> debugTime(final String message, final Mono<T> mono) {
    final AtomicReference<Long> startTime = new AtomicReference<>();
    return mono.doOnSubscribe(x -> {
      System.out.println("Start\t" + message);
      startTime.set(System.nanoTime());
    }).doFinally(x -> {
      System.out.println("End\t" + message + "\t"
        + TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime.get()) + " ms.");
    });
  }

  public static <R extends AutoCloseable, V> Flux<V> fluxCloseable(final Callable<R> supplier,
    final Function<R, Publisher<V>> mapper) {
    return BaseCloseable.fluxUsing(supplier, mapper);
  }

  public static <IN, OUT> Flux<OUT> fluxCreate(final Publisher<IN> source,
    final Function<FluxSink<OUT>, BaseSubscriber<IN>> subscriberConstructor) {
    return Flux.create(sink -> {
      final Flux<IN> flux = Flux.from(source);
      final BaseSubscriber<IN> subscriber = subscriberConstructor.apply(sink);
      sink.onDispose(subscriber);
      flux.subscribe(subscriber);
    });
  }

  public static <V> MergeSinkHandler<V> merge(final Publisher<V> source1,
    final Publisher<V> source2, final Comparator<V> comparator) {
    return new MergeSinkHandler<V>(source1, source2, comparator);
  }

  public static <R extends AutoCloseable, V> Mono<V> monoCloseable(final Callable<R> supplier,
    final Function<R, Mono<V>> mapper) {
    return BaseCloseable.monoUsing(supplier, mapper);
  }

  public static <T> Mono<T> monoJust(final T value, final Consumer<T> discarder) {
    @SuppressWarnings({
      "rawtypes", "unchecked"
    })
    final Class<? extends T> clazz = (Class)value.getClass();
    return Mono.just(value).doOnDiscard(clazz, discarder);
  }

  public static <T> Consumer<T> once(final Runnable action) {
    return new Consumer<T>() {
      boolean first = true;

      @Override
      public void accept(final T t) {
        if (this.first) {
          this.first = false;
          action.run();
        }
      }
    };
  }

  public static <V> Flux<Pair<V, V>> pair(final Publisher<V> source1, final Publisher<V> source2,
    final Comparator<V> comparator) {
    return Flux.create(sink -> new PairSinkHandler<>(sink, source1, source2, comparator));
  }

  public static void waitOn(final Flux<?> publisher) {
    waitOn(publisher, NOOPCALLBACK);
  }

  public static void waitOn(final Flux<?> publisher,
    final Consumer<Disposable> subscriptionCallback) {
    final Function<CountDownLatch, Disposable> supplier = latch -> publisher
      .doAfterTerminate(latch::countDown)
      .subscribe();
    waitOn(supplier, subscriptionCallback);
  }

  private static void waitOn(final Function<CountDownLatch, Disposable> subscriptionSupplier,
    final Consumer<? super Disposable> subscriptionCallback) {
    final CountDownLatch latch = new CountDownLatch(1);
    final Disposable subscription = subscriptionSupplier.apply(latch);
    final Disposable latchDisposable = new LatchDisposable(latch, subscription);
    subscriptionCallback.accept(latchDisposable);
    try {
      latch.await();
    } catch (final InterruptedException e) {
      throw Exceptions.wrap(e);
    }
  }

  public static void waitOn(final Mono<?> publisher) {
    waitOn(publisher, NOOPCALLBACK);
  }

  public static void waitOn(final Mono<?> publisher,
    final Consumer<Disposable> subscriptionCallback) {
    final Function<CountDownLatch, Disposable> supplier = latch -> publisher
      .doAfterTerminate(latch::countDown)
      .subscribe();
    waitOn(supplier, subscriptionCallback);
  }

  public static void waitOn(final ParallelFlux<?> publisher) {
    waitOn(publisher, NOOPCALLBACK);
  }

  public static void waitOn(final ParallelFlux<?> publisher,
    final Consumer<Disposable> subscriptionCallback) {
    final Function<CountDownLatch, Disposable> supplier = latch -> publisher
      .doAfterTerminate(latch::countDown)
      .subscribe();
    waitOn(supplier, subscriptionCallback);
  }

  public static <V> void waitOnAction(final Flux<V> publisher, final Consumer<V> action) {
    waitOnAction(publisher, action, NOOPCALLBACK);
  }

  public static <V> void waitOnAction(final Flux<V> publisher, final Consumer<V> action,
    final Consumer<Disposable> subscriptionCallback) {
    final Function<CountDownLatch, Disposable> supplier = latch -> publisher
      .doAfterTerminate(latch::countDown)
      .subscribe(action);
    waitOn(supplier, subscriptionCallback);
  }

  public static <V> void waitOnAction(final Mono<V> publisher, final Consumer<V> action) {
    waitOnAction(publisher, action, NOOPCALLBACK);
  }

  public static <V> void waitOnAction(final Mono<V> publisher, final Consumer<V> action,
    final Consumer<Disposable> subscriptionCallback) {
    final Function<CountDownLatch, Disposable> supplier = latch -> publisher
      .doAfterTerminate(latch::countDown)
      .subscribe(action);
    waitOn(supplier, subscriptionCallback);
  }

  public static <V> void waitOnAction(final ParallelFlux<V> publisher, final Consumer<V> action) {
    waitOnAction(publisher, action, NOOPCALLBACK);
  }

  public static <V> void waitOnAction(final ParallelFlux<V> publisher, final Consumer<V> action,
    final Consumer<Disposable> subscriptionCallback) {
    final Function<CountDownLatch, Disposable> supplier = latch -> publisher
      .doAfterTerminate(latch::countDown)
      .subscribe(action);
    waitOn(supplier, subscriptionCallback);
  }

  public static <R> Flux<R> withTempFileFlux(final Path file,
    final Function<Path, Flux<R>> action) {
    return withTempFileFlux(file, action, false);
  }

  public static <R> Flux<R> withTempFileFlux(final Path file, final Function<Path, Flux<R>> action,
    final boolean deleteIfEmpty) {

    final Callable<Path> resource = () -> {
      return file.getParent().resolve("_" + file.getFileName());
    };

    Function<Path, Flux<R>> publisher = (tempFile) -> action.apply(tempFile)
      .doOnError((e) -> Paths.deleteDirectories(tempFile));

    final Consumer<Path> closer = (tempPath) -> {
      try {
        if (Paths.exists(tempPath)) {
          Files.move(tempPath, file, StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    };
    if (deleteIfEmpty) {
      final Function<Path, Flux<R>> p = publisher;
      publisher = (tempFile) -> p.apply(tempFile)
        .switchIfEmpty(Mono.<R> empty().doOnTerminate(() -> Paths.deleteDirectories(tempFile)));
    }
    return Flux.using(resource, publisher, closer);
  }

  public static <R> Mono<R> withTempFileMono(final Path file,
    final Function<Path, Mono<R>> action) {

    final Callable<Path> resource = () -> {
      return file.getParent().resolve("_" + file.getFileName());
    };

    final Function<Path, Mono<R>> publisher = (tempFile) -> action.apply(tempFile)
      .doOnError((e) -> Paths.deleteDirectories(tempFile));

    final Consumer<Path> closer = (tempPath) -> {
      try {
        if (Paths.exists(tempPath)) {
          Files.move(tempPath, file, StandardCopyOption.ATOMIC_MOVE,
            StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (final IOException e) {
        throw Exceptions.wrap(e);
      }
    };

    return Mono.using(resource, publisher, closer);
  }

}
