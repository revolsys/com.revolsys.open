package com.revolsys.parallel.process;

import org.apache.log4j.Logger;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.store.Buffer;

public abstract class AbstractInOutProcess<I, O> extends AbstractProcess
  implements InOutProcess<I, O> {

  private Channel<I> in;

  private int inBufferSize = 0;

  private Channel<O> out;

  private int outBufferSize = 0;

  public AbstractInOutProcess() {
  }

  public AbstractInOutProcess(final Channel<I> in, final Channel<O> out) {
    this.in = in;
    this.out = out;
  }

  protected void destroy() {
  }

  /**
   * @return the in
   */
  public Channel<I> getIn() {
    if (in == null) {
      final String channelName = getBeanName() + ".in";
      if (inBufferSize < 1) {
        final Channel<I> channel = new Channel<I>(channelName);
        setIn(channel);
      } else {

        final Buffer<I> buffer = new Buffer<I>(inBufferSize);
        final Channel<I> channel = new Channel<I>(channelName, buffer);
        setIn(channel);
      }
    }
    return in;
  }

  /**
   * @return the out
   */
  public Channel<O> getOut() {
    if (out == null) {
      final String channelName = getBeanName() + ".out";
      if (outBufferSize < 1) {
        setOut(new Channel<O>(channelName));
      } else {
        setOut(new Channel<O>(channelName, new Buffer<O>(outBufferSize)));
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

  protected abstract void run(Channel<I> in, Channel<O> out);

  /**
   * @param in the in to set
   */
  public void setIn(final Channel<I> in) {
    this.in = in;
    in.readConnect();
  }

  /**
   * @param out the out to set
   */
  public void setOut(final Channel<O> out) {
    this.out = out;
    out.writeConnect();
  }

  public int getInBufferSize() {
    return inBufferSize;
  }

  public void setInBufferSize(int inBufferSize) {
    this.inBufferSize = inBufferSize;
  }

  public int getOutBufferSize() {
    return outBufferSize;
  }

  public void setOutBufferSize(int outBufferSize) {
    this.outBufferSize = outBufferSize;
  }

}
