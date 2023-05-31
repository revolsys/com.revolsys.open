package com.revolsys.reactive;

import java.util.Arrays;
import java.util.Comparator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jeometry.common.exception.Exceptions;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.revolsys.util.Pair;

import reactor.core.publisher.FluxSink;

public class PairSinkHandler<V> {

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
      this.state.compareAndSet(SourceSubscriberState.NEXT_REQUEST, SourceSubscriberState.COMPLETE);
      emit();
    }

    @Override
    public void onError(final Throwable t) {
      PairSinkHandler.this.sink.error(t);
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

  private enum SourceSubscriberState {
    SUBSCRIBE_START, SUBSCRIBE_SET, EMPTY, NEXT_REQUEST, NEXT_SET, FULL, PEEK, COMPLETE
  }

  private final SourceSubscriber subscription1;

  private final SourceSubscriber subscription2;

  private final AtomicLong requestedCount = new AtomicLong();

  private FluxSink<Pair<V, V>> sink;

  private final Comparator<V> comparator;

  public PairSinkHandler(final FluxSink<Pair<V, V>> sink, final Publisher<V> publisher1,
    final Publisher<V> publisher2, final Comparator<V> comparator) {
    this.sink = sink;
    this.subscription1 = new SourceSubscriber(publisher1);
    this.subscription2 = new SourceSubscriber(publisher2);
    this.comparator = comparator;
    sink.onCancel(this::cancel);
    sink.onDispose(this::dispose);
    sink.onRequest(this::onRequest);
  }

  private void cancel() {
    Throwable savedE = null;
    for (final SourceSubscriber subscription : Arrays.asList(this.subscription1,
      this.subscription2)) {
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
    final SourceSubscriber sub1 = this.subscription1;
    final SourceSubscriber sub2 = this.subscription2;
    Pair<V, V> pair = null;

    final V value1 = sub1.peek();
    final V value2 = sub2.peek();
    if (value1 == null) {
      if (value2 == null) {
        if (sub1.isComplete() && sub2.isComplete()) {
          this.sink.complete();
          return;
        }
      } else {
        if (sub1.isComplete()) {
          pair = new Pair<>(null, value2);
          sub2.pop();
        } else {
          sub2.unpeek();
        }
      }
    } else {
      if (value2 == null) {
        if (sub2.isComplete()) {
          pair = new Pair<>(value1, null);
          sub1.pop();
        } else {
          sub1.unpeek();
        }
      } else {
        final int compare = this.comparator.compare(value1, value2);
        if (compare < 0) {
          pair = new Pair<>(value1, null);
          sub1.pop();
          sub2.unpeek();
        } else if (compare == 0) {
          pair = new Pair<>(value1, value2);
          sub1.pop();
          sub2.pop();
        } else {
          pair = new Pair<>(null, value2);
          sub1.unpeek();
          sub2.pop();
        }

      }
    }
    if (pair != null) {
      this.sink.next(pair);
    }
    if (requestNext()) {
      this.subscription1.requestNext();
      this.subscription2.requestNext();
    }

    if (sub1.isComplete() && sub2.isComplete()) {
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

  public void setSink(final FluxSink<Pair<V, V>> sink) {
    this.sink = sink;
    this.sink.onCancel(this::cancel);
    this.sink.onDispose(this::dispose);
    this.sink.onRequest(this::onRequest);
  }

  @Override
  public String toString() {
    return this.subscription1 + "\n" + this.subscription2;
  }
}
