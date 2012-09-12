package com.revolsys.parallel.process;

import org.apache.log4j.Logger;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.store.Buffer;

public abstract class AbstractOutProcess<T> extends AbstractProcess implements
  OutProcess<T> {

  private int outBufferSize = 0;

  private Channel<T> out;

  public AbstractOutProcess() {
  }

  public AbstractOutProcess(final Channel<T> out) {
    this.out = out;
  }

  public AbstractOutProcess(final int outBufferSize) {
    this.outBufferSize = outBufferSize;
  }

  protected void destroy() {
  }

  /**
   * @return the out
   */
  @Override
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

  public int getOutBufferSize() {
    return outBufferSize;
  }

  protected void init() {
  }

  @Override
  public final void run() {
    final Logger log = Logger.getLogger(getClass());
    try {
      log.debug("Start");
      init();
      run(getOut());
    } catch (final ClosedException e) {
      log.debug("Shutdown");
    } catch (final ThreadDeath e) {
      log.debug("Shutdown");
    } catch (final Throwable e) {
      log.error(e.getMessage(), e);
      getProcessNetwork().stop();
    } finally {
      if (out != null) {
        out.writeDisconnect();
      }
      destroy();
    }
  }

  protected abstract void run(final Channel<T> out);

  /**
   * @param out the out to set
   */
  @Override
  public void setOut(final Channel<T> out) {
    this.out = out;
    out.writeConnect();

  }

  public void setOutBufferSize(final int outBufferSize) {
    this.outBufferSize = outBufferSize;
  }

}
