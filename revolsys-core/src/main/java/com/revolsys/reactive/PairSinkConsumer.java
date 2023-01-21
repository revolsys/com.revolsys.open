package com.revolsys.reactive;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.jeometry.common.exception.Exceptions;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.revolsys.util.Pair;

import reactor.core.publisher.FluxSink;

public class PairSinkConsumer<V> implements Consumer<FluxSink<Pair<V, V>>> {

  private class PairSinkState {
    private class PairSinkStateSubscriber implements Subscriber<V> {

      private Subscription subscription;

      private V currentValue;

      private boolean requestingNext = false;

      public PairSinkStateSubscriber(final Publisher<V> publisher) {
        publisher.subscribe(this);
      }

      public V aquireCurrentValue() {
        final V value = this.currentValue;
        this.currentValue = null;
        return value;
      }

      public void cancel() {
        this.currentValue = null;
        final Subscription subscription = this.subscription;
        this.subscription = null;
        if (subscription != null) {
          subscription.cancel();
        }
      }

      public V clearCurrentValue() {
        this.currentValue = null;
        return null;
      }

      public boolean isOpen() {
        return this.subscription != null;
      }

      public boolean isRequestingNext() {
        return this.requestingNext;
      }

      @Override
      public void onComplete() {
        this.subscription = null;
        emit();
      }

      @Override
      public void onError(final Throwable t) {
        PairSinkState.this.sink.error(t);
      }

      @Override
      public void onNext(final V value) {
        if (this.currentValue != null || !this.requestingNext) {
          throw new IllegalStateException("Cannot overwrite value");
        }
        this.requestingNext = false;
        this.currentValue = value;
        emit();
      }

      @Override
      public void onSubscribe(final Subscription subscription) {
        this.subscription = subscription;
      }

      public V peekCurrentValue() {
        return this.currentValue;
      }

      public void requestNext() {
        this.subscription.request(1);
      }

      public void setRequestingNext() {
        this.requestingNext = true;
      }
    }

    private final PairSinkStateSubscriber subscription1;

    private final PairSinkStateSubscriber subscription2;

    private final AtomicLong requestedCount = new AtomicLong();

    private final FluxSink<Pair<V, V>> sink;

    public PairSinkState(final FluxSink<Pair<V, V>> sink) {
      this.sink = sink;
      this.subscription1 = new PairSinkStateSubscriber(PairSinkConsumer.this.publishers.get(0));
      this.subscription2 = new PairSinkStateSubscriber(PairSinkConsumer.this.publishers.get(1));
    }

    private void cancel() {
      Throwable savedE = null;
      for (final PairSinkStateSubscriber subscription : Arrays.asList(this.subscription1,
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

    private void emit() {
      final PairSinkStateSubscriber sub1 = this.subscription1;
      final PairSinkStateSubscriber sub2 = this.subscription2;
      if (sub1 != null && sub2 != null) {
        final boolean open1 = sub1.isOpen();
        final boolean open2 = sub2.isOpen();
        if (open1) {
          if (open2) {
            if (!(sub1.isRequestingNext() || sub2.isRequestingNext())) {
              V value1 = sub1.peekCurrentValue();
              V value2 = sub2.peekCurrentValue();
              if (value1 != null && value2 != null) {
                final int compare = PairSinkConsumer.this.comparator.compare(value1, value2);
                final Pair<V, V> pair;
                if (compare < 0) {
                  pair = new Pair<>(value1, null);
                  value1 = sub1.clearCurrentValue();
                } else if (compare == 0) {
                  pair = new Pair<>(value1, value2);
                  value1 = sub1.clearCurrentValue();
                  value2 = sub2.clearCurrentValue();
                } else {
                  pair = new Pair<>(null, value2);
                  value2 = sub2.clearCurrentValue();
                }
                this.sink.next(pair);
              }
              if (requestNext()) {
                if (value1 == null) {
                  this.subscription1.setRequestingNext();
                }
                if (value2 == null) {
                  this.subscription2.setRequestingNext();
                }
                if (value1 == null) {
                  this.subscription1.requestNext();
                }
                if (value2 == null) {
                  this.subscription2.requestNext();
                }
              }
            }
          } else {
            emitAll(0, sub1);
          }
        } else {
          if (open2) {
            emitAll(1, sub2);
          } else {
            this.sink.complete();
          }
        }
      }
    }

    private void emitAll(final int i, final PairSinkStateSubscriber subscription) {
      final V value = subscription.aquireCurrentValue();
      if (value != null) {
        final Pair<V, V> pair;
        if (i == 0) {
          pair = new Pair<>(value, null);
        } else {
          pair = new Pair<>(null, value);
        }
        this.sink.next(pair);
      }
      if (requestNext()) {
        subscription.requestNext();
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

  private final List<Publisher<V>> publishers;

  private final Comparator<V> comparator;

  public PairSinkConsumer(final Publisher<V> publisher1, final Publisher<V> publisher2,
    final Comparator<V> comparator) {
    this.publishers = Arrays.asList(publisher1, publisher2);
    this.comparator = comparator;
  }

  @Override
  public void accept(final FluxSink<Pair<V, V>> sink) {
    final PairSinkState state = new PairSinkState(sink);
    sink.onCancel(state::cancel).onDispose(state::dispose).onRequest(state::onRequest);
  }

}
