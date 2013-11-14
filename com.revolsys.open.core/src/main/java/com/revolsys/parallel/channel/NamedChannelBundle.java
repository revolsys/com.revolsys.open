package com.revolsys.parallel.channel;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicLong;

import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.ThreadUtil;

public class NamedChannelBundle<T> {

  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** The ChannelDataStore used to store the valueQueueByName for the Channel */
  protected Map<String, Queue<T>> valueQueueByName = new HashMap<String, Queue<T>>();

  private AtomicLong sequence = new AtomicLong();

  private Map<String, Queue<Long>> sequenceQueueByName = new HashMap<String, Queue<Long>>();

  /** The monitor reads must synchronize on */
  private final Object monitor = new Object();

  /** The name of the channel. */
  private String name;

  /** Number of readers connected to the channel. */
  private int numReaders = 0;

  /** Number of writers connected to the channel. */
  private int numWriters = 0;

  /** The monitor reads must synchronize on */
  private final Object readMonitor = new Object();

  /** Flag indicating if the channel is closed for writing. */
  private boolean writeClosed;

  /** The monitor writes must synchronize on */
  private final Object writeMonitor = new Object();

  public NamedChannelBundle() {
  }

  public NamedChannelBundle(final String name) {
    this.name = name;
  }

  public void close() {
    closed = true;
    synchronized (monitor) {
      valueQueueByName = null;
      sequence = null;
      sequenceQueueByName = null;
      monitor.notifyAll();
    }
  }

  public String getName() {
    return name;
  }

  private Queue<T> getNextValueQueue(Collection<String> names) {
    String selectedName = null;
    long lowestSequence = Long.MAX_VALUE;
    if (names == null) {
      names = sequenceQueueByName.keySet();
    }
    for (final String name : names) {
      final Queue<Long> sequenceQueue = sequenceQueueByName.get(name);
      if (sequenceQueue != null && !sequenceQueue.isEmpty()) {
        final long sequence = sequenceQueue.peek();
        if (sequence < lowestSequence) {
          lowestSequence = sequence;
          selectedName = name;
        }
      }
    }
    if (selectedName == null) {
      return null;
    } else {
      final Queue<Long> sequenceQueue = sequenceQueueByName.get(selectedName);
      sequenceQueue.remove();
      return getValueQueue(selectedName);
    }
  }

  private Queue<Long> getSequenceQueue(final String name) {
    Queue<Long> queue = sequenceQueueByName.get(name);
    if (queue == null) {
      queue = new LinkedList<Long>();
      sequenceQueueByName.put(name, queue);
    }
    return queue;
  }

  private Queue<T> getValueQueue(final String name) {
    Queue<T> queue = valueQueueByName.get(name);
    if (queue == null) {
      queue = new LinkedList<T>();
      valueQueueByName.put(name, queue);
    }
    return queue;
  }

  public boolean isClosed() {
    if (!closed) {
      if (writeClosed) {
        boolean empty = true;
        synchronized (monitor) {
          for (final Queue<T> queue : valueQueueByName.values()) {
            if (!queue.isEmpty()) {
              empty = false;
            }
          }
          if (empty) {
            close();
          }
        }
      }
    }

    return closed;
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read.
   * 
   * @return The object returned from the Channel.
   */
  public T read() {
    return read(0, Collections.<String> emptyList());
  }

  public T read(final Collection<String> names) {
    return read(0, names);
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read. If no data is available to be read after the
   * timeout the method will return null.
   * 
   * @param timeout The maximum time to wait in milliseconds.
   * @return The object returned from the Channel.
   */
  public T read(final long timeout) {
    return read(timeout, Collections.<String> emptyList());
  }

  public T read(final long timeout, final Collection<String> names) {
    synchronized (readMonitor) {
      synchronized (monitor) {
        try {
          long maxTime = 0;
          if (timeout > 0) {
            maxTime = System.currentTimeMillis() + timeout;
          }
          if (isClosed()) {
            throw new ClosedException();
          }
          Queue<T> queue = getNextValueQueue(names);
          if (timeout == 0) {
            while (queue == null) {
              ThreadUtil.pause(monitor);
              if (isClosed()) {
                throw new ClosedException();
              }

              queue = getNextValueQueue(names);
            }
          } else if (timeout > 0) {
            long waitTime = maxTime - System.currentTimeMillis();
            while (queue == null && waitTime > 0) {
              ThreadUtil.pause(monitor, waitTime);
              if (isClosed()) {
                throw new ClosedException();
              }

              queue = getNextValueQueue(names);
              waitTime = maxTime - System.currentTimeMillis();
            }
          } else {
            queue = getNextValueQueue(names);
          }
          if (queue == null) {
            return null;
          } else {
            final T value = queue.remove();
            monitor.notifyAll();
            return value;
          }
        } catch (final ThreadInterruptedException e) {
          close();
          monitor.notifyAll();
          throw new ClosedException();
        }
      }
    }
  }

  public T read(final long timeout, final String... names) {
    return read(timeout, Arrays.asList(names));
  }

  public T read(final String... names) {
    return read(0, Arrays.asList(names));
  }

  public void readConnect() {
    synchronized (monitor) {
      if (isClosed()) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        numReaders++;
      }

    }
  }

  public void readDisconnect() {
    synchronized (monitor) {
      if (!closed) {
        numReaders--;
        if (numReaders <= 0) {
          close();
          monitor.notifyAll();
        }
      }

    }
  }

  public Collection<T> remove(final String name) {
    synchronized (monitor) {
      sequenceQueueByName.remove(name);
      final Queue<T> values = valueQueueByName.remove(name);
      monitor.notifyAll();
      return values;
    }
  }

  /**
   * Writes a named Object to the Channel. This method also ensures only one of
   * the writers can actually be writing at any time. All other writers are
   * blocked until it completes the write. The channel can never be full so it
   * does not block on write.
   * 
   * @param value The object to write to the Channel.
   */
  public void write(final String name, final T value) {
    synchronized (writeMonitor) {
      synchronized (monitor) {
        if (closed) {
          monitor.notifyAll();
          throw new ClosedException();
        } else {
          final Long sequence = this.sequence.getAndIncrement();
          final Queue<Long> sequenceQueue = getSequenceQueue(name);
          sequenceQueue.add(sequence);

          final Queue<T> queue = getValueQueue(name);
          queue.add(value);

          monitor.notifyAll();
        }
      }
    }
  }

  public void writeConnect() {
    synchronized (monitor) {
      if (writeClosed) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        numWriters++;
      }

    }
  }

  public void writeDisconnect() {
    synchronized (monitor) {
      if (!writeClosed) {
        numWriters--;
        if (numWriters <= 0) {
          writeClosed = true;
          monitor.notifyAll();
        }
      }
    }
  }
}
