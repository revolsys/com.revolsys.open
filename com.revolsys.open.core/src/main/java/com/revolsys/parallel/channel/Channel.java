package com.revolsys.parallel.channel;

import java.util.Iterator;

import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.ThreadUtil;
import com.revolsys.parallel.channel.store.ZeroBuffer;

public class Channel<T> implements SelectableChannelInput<T>, ChannelOutput<T> {
  /** The Alternative class which will control the selection */
  protected MultiInputSelector alt;

  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** The ChannelValueStore used to store the data for the Channel */
  protected ChannelValueStore<T> data;

  /** The monitor reads must synchronize on */
  protected Object monitor = new Object();

  /** The name of the channel. */
  private String name;

  /** Number of readers connected to the channel. */
  private int numReaders = 0;

  /** Number of writers connected to the channel. */
  private int numWriters = 0;

  /** The monitor reads must synchronize on */
  protected Object readMonitor = new Object();

  /** Flag indicating if the channel is closed for writing. */
  private boolean writeClosed;

  /** The monitor writes must synchronize on */
  protected Object writeMonitor = new Object();

  /**
   * Constructs a new Channel<T> with a ZeroBuffer ChannelValueStore.
   */
  public Channel() {
    this(new ZeroBuffer<T>());
  }

  /**
   * Constructs a new Channel<T> with the specified ChannelValueStore.
   * 
   * @param data The ChannelValueStore used to store the data for the Channel
   */
  public Channel(final ChannelValueStore<T> data) {
    this.data = data;
  }

  public Channel(final String name) {
    this();
    this.name = name;
  }

  public Channel(final String name, final ChannelValueStore<T> data) {
    this.name = name;
    this.data = data;
  }

  public void close() {
    closed = true;
  }

  @Override
  public boolean disable() {
    alt = null;
    return (data.getState() != ChannelValueStore.EMPTY);
  }

  @Override
  public boolean enable(final MultiInputSelector alt) {
    synchronized (monitor) {
      if (data.getState() == ChannelValueStore.EMPTY) {
        this.alt = alt;
        return false;
      } else {
        return true;
      }
    }
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean isClosed() {
    if (!closed) {
      if (writeClosed) {
        if (data.getState() == ChannelValueStore.EMPTY) {
          close();
        }
      }
    }

    return closed;
  }

  @Override
  public Iterator<T> iterator() {
    return new ChannelInputIterator<T>(this);
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read.
   * 
   * @return The object returned from the Channel.
   */
  @Override
  public T read() {
    return read(0);
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
  @Override
  public T read(final long timeout) {
    synchronized (readMonitor) {
      synchronized (monitor) {
        if (isClosed()) {
          throw new ClosedException();
        }
        if (data.getState() == ChannelValueStore.EMPTY) {
          try {
            ThreadUtil.pause(monitor, timeout);
            if (isClosed()) {
              throw new ClosedException();
            }
          } catch (final ThreadInterruptedException e) {
            close();
            monitor.notifyAll();
            throw new ClosedException();
          }
        }
        if (data.getState() == ChannelValueStore.EMPTY) {
          return null;
        } else {
          final T value = data.get();
          monitor.notifyAll();
          return value;
        }
      }
    }
  }

  @Override
  public void readConnect() {
    synchronized (monitor) {
      if (isClosed()) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        numReaders++;
      }

    }
  }

  @Override
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

  @Override
  public String toString() {
    if (name == null) {
      return data.toString();
    } else {
      return name;
    }
  }

  /**
   * Writes an Object to the Channel. This method also ensures only one of the
   * writers can actually be writing at any time. All other writers are blocked
   * until it completes the write.
   * 
   * @param value The object to write to the Channel.
   */
  @Override
  public void write(final T value) {
    synchronized (writeMonitor) {
      synchronized (monitor) {
        if (closed) {
          throw new ClosedException();
        }
        final MultiInputSelector tempAlt = alt;
        data.put(value);
        if (tempAlt != null) {
          tempAlt.schedule();
        } else {
          monitor.notifyAll();
        }
        if (data.getState() == ChannelValueStore.FULL) {
          try {
            ThreadUtil.pause(monitor);
            if (closed) {
              throw new ClosedException();
            }
          } catch (final ThreadInterruptedException e) {
            close();
            monitor.notifyAll();
            throw new ClosedException(e);
          }
        }
      }
    }
  }

  @Override
  public void writeConnect() {
    synchronized (monitor) {
      if (writeClosed) {
        throw new IllegalStateException("Cannot connect to a closed channel");
      } else {
        numWriters++;
      }

    }
  }

  @Override
  public void writeDisconnect() {
    synchronized (monitor) {
      if (!writeClosed) {
        numWriters--;
        if (numWriters <= 0) {
          writeClosed = true;
          final MultiInputSelector tempAlt = alt;
          if (tempAlt != null) {
            tempAlt.closeChannel();
          } else {
            monitor.notifyAll();
          }
        }
      }

    }
  }
}
