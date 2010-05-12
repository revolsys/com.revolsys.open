package com.revolsys.parallel.process;

import org.apache.log4j.Logger;

import com.revolsys.parallel.channel.Buffer;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public abstract class AbstractInProcess<T> extends AbstractProcess implements
  InProcess<T> {

  private int inBufferSize = 0;

  private Channel<T> in;

  public AbstractInProcess() {
  }

  public AbstractInProcess(
    final Channel<T> in) {
    this.in = in;
  }

  public AbstractInProcess(
    int bufferSize) {
    this.inBufferSize = bufferSize;
  }

  /**
   * @return the bufferSize
   */
  public int getInBufferSize() {
    return inBufferSize;
  }

  /**
   * @param bufferSize the bufferSize to set
   */
  public void setInBufferSize(
    int inBufferSize) {
    this.inBufferSize = inBufferSize;
  }

  /**
   * @return the in
   */
  public Channel<T> getIn() {
    if (in == null) {
      final String channelName = getBeanName() + ".in";
      if (inBufferSize == 0) {
        setIn(new Channel<T>(channelName));
      } else {
        setIn(new Channel<T>(channelName, new Buffer<T>(inBufferSize)));
      }
    }
    return in;
  }

  /**
   * @param in the in to set
   */
  public void setIn(
    final Channel<T> in) {
    this.in = in;
    in.readConnect();
  }

  public final void run() {
    Logger log = Logger.getLogger(getClass());
    try {
      log.debug("Start");
      init();
      run(getIn());
    } catch (ThreadDeath e) {
      log.debug("Shutdown");
    } catch (ClosedException e) {
      log.debug("Shutdown");
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      getProcessNetwork().stop();
    } finally {
      if (in != null) {
        in.readDisconnect();
      }
      destroy();
    }
  }

  protected abstract void run(
    Channel<T> in);

  protected void init() {
  }

  protected void destroy() {
  }
}
