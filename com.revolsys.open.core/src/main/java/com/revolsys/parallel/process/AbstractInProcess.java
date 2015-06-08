package com.revolsys.parallel.process;

import org.apache.log4j.Logger;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.store.Buffer;

public abstract class AbstractInProcess<T> extends AbstractProcess implements InProcess<T> {

  private int inBufferSize = 0;

  private Channel<T> in;

  public AbstractInProcess() {
  }

  public AbstractInProcess(final Channel<T> in) {
    this.in = in;
  }

  public AbstractInProcess(final int bufferSize) {
    this.inBufferSize = bufferSize;
  }

  protected void destroy() {
  }

  /**
   * @return the in
   */
  @Override
  public Channel<T> getIn() {
    if (this.in == null) {
      final String channelName = getBeanName() + ".in";
      if (this.inBufferSize == 0) {
        setIn(new Channel<T>(channelName));
      } else {
        setIn(new Channel<T>(channelName, new Buffer<T>(this.inBufferSize)));
      }
    }
    return this.in;
  }

  /**
   * @return the bufferSize
   */
  public int getInBufferSize() {
    return this.inBufferSize;
  }

  protected void init() {
  }

  @Override
  public final void run() {
    final Logger log = Logger.getLogger(getClass());
    try {
      log.debug("Start");
      init();
      run(getIn());
    } catch (final ThreadDeath e) {
      log.debug("Shutdown");
    } catch (final ClosedException e) {
      log.debug("Shutdown");
    } catch (final Throwable e) {
      log.error(e.getMessage(), e);
      getProcessNetwork().stop();
    } finally {
      if (this.in != null) {
        this.in.readDisconnect();
      }
      destroy();
    }
  }

  protected abstract void run(Channel<T> in);

  /**
   * @param in the in to set
   */
  @Override
  public void setIn(final Channel<T> in) {
    this.in = in;
    in.readConnect();
  }

  /**
   * @param bufferSize the bufferSize to set
   */
  public void setInBufferSize(final int inBufferSize) {
    this.inBufferSize = inBufferSize;
  }
}
