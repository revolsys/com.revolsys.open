package com.revolsys.parallel.process;

import org.apache.log4j.Logger;

import com.revolsys.parallel.channel.Buffer;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public abstract class AbstractOutProcess<T> extends AbstractProcess implements
  OutProcess<T> {

  private int outBufferSize = 0;

  private Channel<T> out;

  public AbstractOutProcess() {
  }

  public AbstractOutProcess(
    final Channel<T> out) {
    this.out = out;
  }

  public AbstractOutProcess(
    int outBufferSize) {
    this.outBufferSize = outBufferSize;
  }

  /**
   * @return the out
   */
  public Channel<T> getOut() {
    if (out == null) {
      final String channelName = getBeanName() + ".out";
      if (outBufferSize == 0) {
        final Channel<T> channel = new Channel<T>(channelName);
        setOut(channel);
      } else {
        final Buffer<T> buffer = new Buffer<T>(outBufferSize);
        final Channel<T> channel = new Channel<T>(channelName, buffer);
        setOut(channel);
      }
    }
    return out;
  }

  /**
   * @param out the out to set
   */
  public void setOut(
    final Channel<T> out) {
    this.out = out;
    out.writeConnect();

  }

  public final void run() {
    Logger log = Logger.getLogger(getClass());
    try {
      log.debug("Start");
      init();
      run(getOut());
    } catch (ClosedException e) {
      log.debug("Shutdown");
    } catch (ThreadDeath e) {
      log.debug("Shutdown");
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
    } finally {
      if (out != null) {
        out.writeDisconnect();
      }
      destroy();
    }
  }

  protected abstract void run(
    final Channel<T> out);

  protected void init() {
  }

  protected void destroy() {
  }

}
