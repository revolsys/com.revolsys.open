package com.revolsys.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.revolsys.collection.map.ThreadSharedAttributes;
import com.revolsys.parallel.process.InvokeMethodRunnable;

public class ExecutorServiceFactory {
  public static ExecutorService getExecutorService() {
    synchronized (SYNC) {
      ExecutorService executorService = ThreadSharedAttributes.getField(KEY);
      if (executorService == null) {
        executorService = Executors.newCachedThreadPool();
        ThreadSharedAttributes.setDefaultAttribute(KEY, executorService);
      }
      return executorService;

    }
  }

  public static final void invokeMethod(final Object object,
    final String methodName, final Object... args) {
    final ExecutorService executorService = getExecutorService();
    final InvokeMethodRunnable task = new InvokeMethodRunnable(object,
      methodName, args);
    executorService.execute(task);
  }

  public static void setDefaultExecutorService(
    final ExecutorService executorService) {
    ThreadSharedAttributes.setDefaultAttribute(KEY, executorService);
  }

  public static void setThreadExecutorService(
    final ExecutorService executorService) {
    ThreadSharedAttributes.setAttribute(KEY, executorService);
  }

  private static final Object SYNC = new Object();

  private static final String KEY = ExecutorServiceFactory.class.getName()
      + ".key";
}
