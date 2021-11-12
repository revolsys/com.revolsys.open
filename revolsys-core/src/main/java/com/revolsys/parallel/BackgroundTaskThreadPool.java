package com.revolsys.parallel;

import java.util.concurrent.Callable;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jeometry.common.data.refresh.RefreshableValue;
import org.jeometry.common.logging.Logs;

public class BackgroundTaskThreadPool {

  private static final RefreshableValue<ThreadPoolExecutor> EXECUTOR = RefreshableValue
    .supplier(() -> new ThreadPoolExecutor(0, 100, 60, TimeUnit.SECONDS,
      new SynchronousQueue<Runnable>(), new NamedThreadFactory(Thread.NORM_PRIORITY, "bgtasks")) {
      @Override
      protected <T extends Object> RunnableFuture<T> newTaskFor(final Callable<T> callable) {
        return super.newTaskFor(() -> {
          try {
            return callable.call();
          } catch (RuntimeException | Error e) {
            Logs.error(BackgroundTaskThreadPool.class, e);
            throw e;
          }
        });
      }

      @Override
      protected <T extends Object> RunnableFuture<T> newTaskFor(final Runnable runnable,
        final T value) {
        return super.newTaskFor(() -> {
          try {
            runnable.run();
          } catch (RuntimeException | Error e) {
            Logs.error(BackgroundTaskThreadPool.class, e);
            throw e;
          }
        }, value);
      }
    });

  public static ThreadPoolExecutor executor() {
    return EXECUTOR.getValue();
  }

}
