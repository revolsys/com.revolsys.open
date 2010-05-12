package com.revolsys.parallel.process;

import org.apache.log4j.Logger;

import com.revolsys.parallel.channel.Buffer;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public abstract class AbstractInOutProcess<T> extends AbstractProcess implements
  InOutProcess<T> {

  private Channel<T> in;

  private int inBufferSize = 0;

  private Channel<T> out;

  private int outBufferSize = 0;

  public AbstractInOutProcess() {
  }

  public AbstractInOutProcess(
    final Channel<T> in,
    final Channel<T> out) {
    this.in = in;
    this.out = out;
  }

  protected void destroy() {
  }

  /**
   * @return the in
   */
  public Channel<T> getIn() {
    if (in == null) {
      final String channelName = getBeanName() + ".in";
      if (inBufferSize < 1) {
        final Channel<T> channel = new Channel<T>(channelName);
        setIn(channel);
      } else {
        
        final Buffer<T> buffer = new Buffer<T>(inBufferSize);
        final Channel<T> channel = new Channel<T>(channelName, buffer);
        setIn(channel);
      }
    }
    return in;
  }

  /**
   * @return the out
   */
  public Channel<T> getOut() {
    if (out == null) {
      final String channelName = getBeanName() + ".out";
      if (outBufferSize < 1) {
        setOut(new Channel<T>(channelName));
      } else {
        setOut(new Channel<T>(channelName, new Buffer<T>(outBufferSize)));
      }
    }
    return out;
  }

  protected void init() {
  }

  public final void run() {
    Logger log = Logger.getLogger(getClass());
    try {
      log.debug("Start");
      init();
      run(in, out);
    } catch (ClosedException e) {
      log.debug("Shutdown");
    } catch (ThreadDeath e) {
      log.debug("Shutdown");
    } catch (Throwable e) {
      log.error(e.getMessage(), e);
      getProcessNetwork().stop();
    } finally {
      if (in != null) {
        in.readDisconnect();
      }
      if (out != null) {
        out.writeDisconnect();
      }
      destroy();
    }
  }

  protected abstract void run(
    Channel<T> in,
    Channel<T> out);

  /**
   * @param in the in to set
   */
  public void setIn(
    final Channel<T> in) {
    this.in = in;
    in.readConnect();
  }

  /**
   * @param out the out to set
   */
  public void setOut(
    final Channel<T> out) {
    this.out = out;
    out.writeConnect();

  }

  public int getInBufferSize() {
    return inBufferSize;
  }

  public void setInBufferSize(
    int inBufferSize) {
    this.inBufferSize = inBufferSize;
  }

  public int getOutBufferSize() {
    return outBufferSize;
  }

  public void setOutBufferSize(
    int outBufferSize) {
    this.outBufferSize = outBufferSize;
  }

}
