package com.revolsys.parallel.channel;

public class Channel<T> implements AltingChannelInput {
  /** The Alternative class which will control the selection */
  protected MultiChannelReadSelector alt;

  /** The ChannelDataStore used to store the data for the Channel */
  protected ChannelDataStore<T> data;

  /** The monitor reads must synchronize on */
  protected Object monitor = new Object();

  /** The monitor reads must synchronize on */
  protected Object readMonitor = new Object();

  /** The monitor writes must synchronize on */
  protected Object writeMonitor = new Object();

  /** Number of writers connected to the channel. */
  private int numWriters = 0;

  /** Number of readers connected to the channel. */
  private int numReaders = 0;

  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** Flag indicating if the channel is closed for writing. */
  private boolean writeClosed;

  /**
   * Constructs a new Channel<T> with a ZeroBuffer ChannelDataStore.
   */
  public Channel() {
    this(new ZeroBuffer<T>());
  }

  /**
   * Constructs a new Channel<T> with the specified ChannelDataStore.
   * 
   * @param data The ChannelDataStore used to store the data for the Channel
   */
  public Channel(ChannelDataStore<T> data) {
    this.data = data;
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read.
   * 
   * @return The object returned from the Channel.
   */
  public T read() {
    return read(0);
  }

  /**
   * Reads an Object from the Channel. This method also ensures only one of the
   * readers can actually be reading at any time. All other readers are blocked
   * until it completes the read. If no data is available to be read after the
   * timeout the method will return.
   * 
   * @return The object returned from the Channel.
   */
  public T read(int timeout) {
    synchronized (readMonitor) {
      synchronized (monitor) {
        if (isClosed()) {
          throw new ClosedException();
        }
        if (data.getState() == ChannelDataStore.EMPTY) {
          try {
            monitor.wait(timeout);
            if (isClosed()) {
              throw new ClosedException();
            }
          } catch (InterruptedException e) {
          }
        }
        if (data.getState() == ChannelDataStore.EMPTY) {
          return null;
        } else {
          T value = data.get();
          monitor.notifyAll();
          return value;
        }
      }
    }
  }

  /**
   * Writes an Object to the Channel. This method also ensures only one of the
   * writers can actually be writing at any time. All other writers are blocked
   * until it completes the write.
   * 
   * @param value The object to write to the Channel.
   */
  public void write(T value) {
    synchronized (writeMonitor) {
      synchronized (monitor) {
        if (closed) {
          throw new ClosedException();
        }
        MultiChannelReadSelector tempAlt = alt;
        data.put(value);
        if (tempAlt != null) {
          tempAlt.schedule();
        } else {
          monitor.notifyAll();
        }
        if (data.getState() == ChannelDataStore.FULL) {
          try {
            monitor.wait();
            if (closed) {
              throw new ClosedException();
            }
          } catch (InterruptedException e) {
          }
        }
      }
    }
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
          MultiChannelReadSelector tempAlt = alt;
          if (tempAlt != null) {
            tempAlt.closeChannel();
          } else {
            monitor.notifyAll();
          }
        }
      }

    }
  }

  public synchronized boolean enable(MultiChannelReadSelector alt) {
    if (data.getState() == ChannelDataStore.EMPTY) {
      this.alt = alt;
      return false;
    } else {
      return true;
    }
  }

  public boolean disable() {
    alt = null;
    return (data.getState() != ChannelDataStore.EMPTY);
  }

  public boolean isClosed() {
    if (!closed) {
      if (writeClosed) {
        if (data.getState() == ChannelDataStore.EMPTY) {
          close();
        }
      }
    }

    return closed;
  }

  public static <T> T[] createArray(T... o) {
    return o;
  }

  public void close() {
    closed = true;
  }
}
