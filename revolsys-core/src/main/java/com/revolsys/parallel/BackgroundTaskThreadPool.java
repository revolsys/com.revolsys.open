package com.revolsys.parallel;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.jeometry.common.data.refresh.RefreshableValue;

public class BackgroundTaskThreadPool {

  private static final RefreshableValue<ThreadPoolExecutor> EXECUTOR = RefreshableValue
    .supplier(() -> new ThreadPoolExecutor(0, 100, 60, TimeUnit.SECONDS,
      new SynchronousQueue<Runnable>(), new NamedThreadFactory(Thread.NORM_PRIORITY, "bgtasks")));

  public static ThreadPoolExecutor executor() {
    return EXECUTOR.getValue();
  }

}
