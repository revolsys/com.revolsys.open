package com.revolsys.parallel;

import java.util.concurrent.Callable;

import com.revolsys.io.BaseCloseable;
import com.revolsys.util.Exceptions;

public class SingleThreadExecutor implements BaseCloseable {
  private static ThreadLocal<Boolean> IS_THREAD = new ThreadLocal<>();

  private final Object callSync = new Object();

  private final Object handleSync = new Object();

  private Throwable exception;

  private Object result;

  private boolean running = true;

  private Callable<? extends Object> task;

  private final Thread thread;

  private final String threadName;

  public SingleThreadExecutor(final String threadName) {
    this.threadName = threadName;
    this.thread = new Thread(this::taskHandler, threadName);
    this.thread.setDaemon(true);
    this.thread.start();
  }

  @SuppressWarnings("unchecked")
  public <V> V call(final Callable<V> task) {
    if (task != null) {
      if (IS_THREAD.get() == Boolean.TRUE) {
        try {
          return task.call();
        } catch (final Exception e) {
          throw Exceptions.wrap(e);
        }
      } else {
        synchronized (this.callSync) {
          synchronized (this.handleSync) {
            try {
              this.task = task;
              this.handleSync.notifyAll();
              this.handleSync.wait();
              if (this.exception == null) {
                return (V)this.result;
              } else {
                throw Exceptions.wrap(this.threadName + ": error running task", this.exception);
              }
            } catch (final InterruptedException e) {
              // Ignore
            } finally {
              this.task = null;
              this.result = null;
              this.exception = null;
            }
          }
        }
      }
    }
    return null;
  }

  @Override
  public void close() {
    if (this.running) {
      this.running = false;
      this.thread.interrupt();
    }
  }

  public boolean isThread() {
    return IS_THREAD.get() == Boolean.TRUE;
  }

  private void taskHandler() {
    IS_THREAD.set(Boolean.TRUE);
    while (this.running) {
      synchronized (this.handleSync) {
        if (this.task == null) {
          try {
            this.handleSync.wait();
          } catch (final InterruptedException e) {
            // Ignore
          }
        } else {
          try {
            this.result = this.task.call();
          } catch (final Throwable e) {
            this.exception = e;
          } finally {
            this.task = null;
          }
          this.handleSync.notifyAll();
        }
      }
    }
  }

  @Override
  public String toString() {
    return this.threadName;
  }
}
