package com.revolsys.parallel;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.Callable;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.util.ExceptionUtil;

public class SingleThreadExecutor implements Closeable {

  private static Channel<Callable<? extends Object>> in = new Channel<>();

  private static Channel<Object> out = new Channel<>();

  private final String threadName;

  private final Thread thread;

  private boolean running = true;

  public SingleThreadExecutor(final String threadName) {
    this.threadName = threadName;
    in.writeConnect();
    out.readConnect();
    this.thread = new Thread(this::taskHandler, threadName);
    this.thread.setDaemon(true);
    this.thread.start();
  }

  @SuppressWarnings("unchecked")
  public <V> V call(final Callable<V> callable) {
    synchronized (this) {
      in.write(callable);
      final V result = (V)out.read();
      return result;
    }
  }

  @Override
  public void close() throws IOException {
    if (this.running) {
      this.running = false;
      in.close();
      in.readDisconnect();
      in.writeDisconnect();

      out.close();
      out.writeDisconnect();
      out.readDisconnect();
      this.thread.interrupt();
    }
  }

  private void taskHandler() {
    in.readConnect();
    out.writeConnect();
    while (this.running) {
      try {
        final Callable<? extends Object> callable = in.read();
        Object result = null;
        try {
          result = callable.call();
        } catch (final Throwable e) {
          ExceptionUtil.log(this.threadName, "Unable to run" + callable, e);
        } finally {
          out.write(result);
        }
      } catch (final ClosedException t) {
        return;
      } catch (final Throwable e) {
        ExceptionUtil.log(this.threadName, "Error getting next task", e);
      }
    }
  }

  @Override
  public String toString() {
    return this.threadName;
  }
}
