package com.revolsys.reactive;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jeometry.common.exception.Exceptions;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

public class MergeSinkHandler<V> {

  private enum SourceSubscriberState {
    SUBSCRIBE_START, SUBSCRIBE_SET, EMPTY, NEXT_REQUEST, NEXT_SET, FULL, PEEK, COMPLETE
  }

  private class State {

    private class SourceSubscriber implements Subscriber<V> {

      private Subscription subscription;

      private V currentValue;

      private final AtomicReference<SourceSubscriberState> state = new AtomicReference<>(
        SourceSubscriberState.SUBSCRIBE_START);

      private final AtomicReference<Boolean> disposed = new AtomicReference<>(false);

      public SourceSubscriber(final Publisher<V> publisher) {
        publisher.subscribe(this);
      }

      public void cancel() {

        if (this.disposed.compareAndSet(false, true)) {
          final Subscription subscription = this.subscription;
          this.subscription = null;
          if (subscription != null) {
            subscription.cancel();
          }
        }
      }

      public boolean isComplete() {
        return this.state.get() == SourceSubscriberState.COMPLETE;
      }

      @Override
      public void onComplete() {
        this.subscription = null;
        this.state.compareAndSet(SourceSubscriberState.NEXT_REQUEST,
          SourceSubscriberState.COMPLETE);
        emit();
      }

      @Override
      public void onError(final Throwable t) {
        State.this.sink.error(t);
      }

      @Override
      public void onNext(final V value) {
        if (this.state.compareAndSet(SourceSubscriberState.NEXT_REQUEST,
          SourceSubscriberState.NEXT_SET)) {
          this.currentValue = value;
          this.state.set(SourceSubscriberState.FULL);
        } else {
          throw new IllegalStateException("onNext can only be called in NEXT_REQUEST state");
        }
        emit();
      }

      @Override
      public void onSubscribe(final Subscription subscription) {
        if (this.state.compareAndSet(SourceSubscriberState.SUBSCRIBE_START,
          SourceSubscriberState.SUBSCRIBE_SET)) {
          this.subscription = subscription;
          this.state.set(SourceSubscriberState.EMPTY);
        } else {
          throw new IllegalStateException("onSubscribe must not be called twice");
        }
      }

      public V peek() {
        if (this.state.compareAndSet(SourceSubscriberState.FULL, SourceSubscriberState.PEEK)) {
          return this.currentValue;
        } else if (this.subscription == null) {
          this.state.compareAndSet(SourceSubscriberState.EMPTY, SourceSubscriberState.COMPLETE);
        }
        return null;
      }

      public Object pop() {
        if (this.state.compareAndSet(SourceSubscriberState.PEEK, SourceSubscriberState.EMPTY)) {
          final Object value = this.currentValue;
          this.currentValue = null;
          if (this.subscription == null) {
            this.state.compareAndSet(SourceSubscriberState.EMPTY, SourceSubscriberState.COMPLETE);
          }
          return value;
        }
        return null;
      }

      public void requestNext() {
        if (this.state.compareAndSet(SourceSubscriberState.EMPTY,
          SourceSubscriberState.NEXT_REQUEST)) {

          this.subscription.request(1);
        }
      }

      @Override
      public String toString() {
        return this.state + "\t" + this.currentValue;
      }

      public void unpeek() {
        this.state.compareAndSet(SourceSubscriberState.PEEK, SourceSubscriberState.FULL);
      }
    }

    private final SourceSubscriber sourceSubscription;

    private final SourceSubscriber targetSubscription;

    private final AtomicLong requestedCount = new AtomicLong();

    private final FluxSink<V> sink;

    public State(final FluxSink<V> sink) {
      this.sourceSubscription = new SourceSubscriber(MergeSinkHandler.this.source);
      this.targetSubscription = new SourceSubscriber(MergeSinkHandler.this.target);
      this.sink = sink;
      sink.onCancel(this::cancel);
      sink.onDispose(this::dispose);
      sink.onRequest(this::onRequest);
    }

    private void cancel() {
      Throwable savedE = null;
      for (final SourceSubscriber subscription : Arrays.asList(this.sourceSubscription,
        this.targetSubscription)) {
        try {
          subscription.cancel();
        } catch (final RuntimeException e) {
          savedE = e;
        } catch (final Error e) {
          savedE = e;
        }
      }
      Exceptions.throwUncheckedException(savedE);
    }

    private void dispose() {
      cancel();
    }

    private synchronized void emit() {
      final SourceSubscriber sourceSubscription = this.sourceSubscription;
      final SourceSubscriber targetSubscription = this.targetSubscription;
      Mono<V> publisher = null;

      final V sourceValue = sourceSubscription.peek();
      final V targetValue = targetSubscription.peek();
      if (sourceValue == null) {
        if (targetValue == null) {
          if (sourceSubscription.isComplete() && targetSubscription.isComplete()) {
            this.sink.complete();
            return;
          }
        } else if (sourceSubscription.isComplete()) {
          publisher = MergeSinkHandler.this.removed$.apply(targetValue);
          targetSubscription.pop();
        } else {
          targetSubscription.unpeek();
        }
      } else if (targetValue == null) {
        if (targetSubscription.isComplete()) {
          publisher = MergeSinkHandler.this.added$.apply(sourceValue);
          sourceSubscription.pop();
        } else {
          sourceSubscription.unpeek();
        }
      } else {
        final int compare = MergeSinkHandler.this.comparator.compare(sourceValue, targetValue);
        if (compare < 0) {
          publisher = MergeSinkHandler.this.added$.apply(sourceValue);
          sourceSubscription.pop();
          targetSubscription.unpeek();
        } else if (compare == 0) {
          publisher = MergeSinkHandler.this.matched$.apply(sourceValue, targetValue);
          sourceSubscription.pop();
          targetSubscription.pop();
        } else {
          publisher = MergeSinkHandler.this.removed$.apply(targetValue);
          sourceSubscription.unpeek();
          targetSubscription.pop();
        }

      }
      if (publisher != null) {
        publisher.doOnError(this.sink::error).subscribe(this.sink::next);
      }
      if (requestNext()) {
        this.sourceSubscription.requestNext();
        this.targetSubscription.requestNext();
      }

      if (sourceSubscription.isComplete() && targetSubscription.isComplete()) {
        this.sink.complete();
      }
    }

    private void onRequest(final long count) {
      if (count < 0) {
        throw new IllegalArgumentException("Count must be >= 0: " + count);
      }
      if (count > 0) {
        synchronized (this.requestedCount) {
          final long oldCount = this.requestedCount.get();
          if (count == Long.MAX_VALUE || this.requestedCount.addAndGet(count) < oldCount) {
            this.requestedCount.set(Long.MAX_VALUE);
          }
        }

      }
      emit();
    }

    private boolean requestNext() {
      synchronized (this.requestedCount) {
        final long count = this.requestedCount.get();
        if (count > 0) {
          if (count != Long.MAX_VALUE) {
          } else {
            this.requestedCount.decrementAndGet();
          }
          return true;
        }
      }
      return false;
    }

  }

  private final Comparator<V> comparator;

  private Function<V, Mono<V>> added$ = Mono::just;

  private BiFunction<V, V, Mono<V>> matched$ = (v1, v2) -> Mono.just(v2);

  private Function<V, Mono<V>> removed$ = Mono::just;

  private final Publisher<V> source;

  private final Publisher<V> target;

  public MergeSinkHandler(final Publisher<V> source, final Publisher<V> target,
    final Comparator<V> comparator) {
    this.source = source;
    this.target = target;
    this.comparator = comparator;
  }

  /**
   * Action to perform when the value is in the source but not the target.
   *
   * @param updateAction The action;
   * @return this
   */
  public MergeSinkHandler<V> added(final Function<V, V> added) {
    this.added$ = v -> {
      final var result = added.apply(v);
      if (result == null) {
        return Mono.empty();
      } else {
        return Mono.just(result);
      }
    };
    return this;
  }

  /**
   * Action to perform when the value is in the source but not the target.
   *
   * @param updateAction The action;
   * @return this
   */
  public MergeSinkHandler<V> added$(final Function<V, Mono<V>> added$) {
    this.added$ = added$;
    return this;
  }

  /**
   * Action to perform when the value is in both the source and target.
   *
   * @param matched
  * @return this
    */
  public MergeSinkHandler<V> matched(final BiFunction<V, V, V> matched) {
    this.matched$ = (s, t) -> {
      final var result = matched.apply(s, t);
      if (result == null) {
        return Mono.empty();
      } else {
        return Mono.just(result);
      }
    };
    return this;
  }

  /**
   * Action to perform when the value is in both the source and target.
   *
   * @param matched$
  * @return this
    */
  public MergeSinkHandler<V> matched$(final BiFunction<V, V, Mono<V>> matched$) {
    this.matched$ = matched$;
    return this;
  }

  /**
   * Action to perform when the value is in the target but not the source.
   *
   * @param updateAction The action;
   * @return this
   */
  public MergeSinkHandler<V> removed(final Function<V, V> removed) {
    this.removed$ = v -> {
      final var result = removed.apply(v);
      if (result == null) {
        return Mono.empty();
      } else {
        return Mono.just(result);
      }
    };
    return this;
  }

  /**
   * Action to perform when the value is in the target but not the source.
   *
   * @param updateAction The action;
   * @return this
   */
  public MergeSinkHandler<V> removed$(final Function<V, Mono<V>> removed$) {
    this.removed$ = removed$;
    return this;
  }

  public Flux<V> toFlux() {
    return Flux.create(State::new);
  }

  @Override
  public String toString() {
    return this.source + "\n" + this.target;
  }
}
