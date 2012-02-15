package com.revolsys.parallel.channel;

public abstract class AbstractChannelOutput<T> implements ChannelOutput<T> {
  /** Flag indicating if the channel has been closed. */
  private boolean closed = false;

  /** The monitor reads must synchronize on */
  private final Object monitor = new Object();

  /** The name of the channel. */
  private String name;

  /** Number of writers connected to the channel. */
  private int numWriters = 0;

  /** Flag indicating if the channel is closed for writing. */
  private boolean writeClosed;

  /** The monitor writes must synchronize on */
  private final Object writeMonitor = new Object();

  /**
   * Constructs a new Channel<T> with a ZeroBuffer ChannelDataStore.
   */
  public AbstractChannelOutput() {
  }

  public AbstractChannelOutput(final String name) {
    this.name = name;
  }

  public void close() {
    closed = true;
  }

  protected abstract void doWrite(T value);

  public String getName() {
    return name;
  }

  public boolean isClosed() {
    return closed;
  }

  @Override
  public String toString() {
    if (name == null) {
      return super.toString();
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
  public void write(final T value) {
    synchronized (writeMonitor) {
      synchronized (monitor) {
        if (closed) {
          throw new ClosedException();
        }
        doWrite(value);
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
        }
      }
    }
  }
}
