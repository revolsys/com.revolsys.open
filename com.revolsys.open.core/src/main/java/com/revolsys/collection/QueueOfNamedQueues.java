package com.revolsys.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class QueueOfNamedQueues<T> {

  private final AtomicInteger sequence = new AtomicInteger();

  private final Map<String, Queue<T>> valuesByName = new HashMap<String, Queue<T>>();

  private final Map<String, Queue<Integer>> sequenceQueuesByName = new HashMap<String, Queue<Integer>>();

  public boolean add(final String name, final T value) {
    if (offer(name, value)) {
      return true;
    } else {
      throw new IllegalStateException("Queue full");
    }
  }

  public void clear() {
    valuesByName.clear();
    sequenceQueuesByName.clear();
  }

  public T element() {
    return element(new ArrayList<String>(sequenceQueuesByName.keySet()));
  }

  public T element(final Collection<String> names) {
    final String selectedName = getNextQueueName(names);
    if (selectedName == null) {
      throw new NoSuchElementException();
    } else {
      return getHead(selectedName);
    }
  }

  public T element(final String... names) {
    return element(Arrays.asList(names));
  }

  private T getHead(final String selectedName) {
    final Queue<T> values = valuesByName.get(selectedName);
    final T value = values.peek();
    return value;
  }

  private String getNextQueueName(final Collection<String> names) {
    String selectedName = null;
    int lowestSequence = Integer.MAX_VALUE;
    for (final String name : names) {
      final Queue<Integer> sequenceQueue = sequenceQueuesByName.get(name);
      if (sequenceQueue != null && !sequenceQueue.isEmpty()) {
        final int sequence = sequenceQueue.peek();
        if (sequence < lowestSequence) {
          lowestSequence = sequence;
          selectedName = name;
        }
      }
    }
    return selectedName;
  }

  public boolean offer(final String name, final T value) {
    final Integer sequence = this.sequence.getAndIncrement();
    Queue<Integer> sequenceQueue = sequenceQueuesByName.get(name);
    if (sequenceQueue == null) {
      sequenceQueue = new LinkedList<Integer>();
      sequenceQueuesByName.put(name, sequenceQueue);
    }
    Queue<T> values = valuesByName.get(name);
    if (values == null) {
      values = new LinkedList<T>();
      valuesByName.put(name, values);
    }
    sequenceQueue.offer(sequence);
    values.offer(value);
    return true;
  }

  public T peek() {
    return peek(new ArrayList<String>(sequenceQueuesByName.keySet()));
  }

  public T peek(final Collection<String> names) {
    final String selectedName = getNextQueueName(names);
    if (selectedName == null) {
      return null;
    } else {
      return getHead(selectedName);
    }
  }

  public T peek(final String... names) {
    return peek(Arrays.asList(names));
  }

  public T poll() {
    return poll(new ArrayList<String>(sequenceQueuesByName.keySet()));
  }

  public T poll(final Collection<String> names) {
    final String selectedName = getNextQueueName(names);
    if (selectedName == null) {
      return null;
    } else {
      return removeHead(selectedName);
    }
  }

  public T poll(final String... names) {
    return poll(Arrays.asList(names));
  }

  public T remove() {
    return remove(new ArrayList<String>(sequenceQueuesByName.keySet()));
  }

  public T remove(final Collection<String> names) {
    final String selectedName = getNextQueueName(names);
    if (selectedName == null) {
      throw new NoSuchElementException();
    } else {
      return removeHead(selectedName);
    }
  }

  public T remove(final String... names) {
    return remove(Arrays.asList(names));
  }

  private T removeHead(final String selectedName) {
    final Queue<Integer> sequenceQueue = sequenceQueuesByName.get(selectedName);
    sequenceQueue.remove();
    final Queue<T> values = valuesByName.get(selectedName);
    final T value = values.remove();
    return value;
  }

}
