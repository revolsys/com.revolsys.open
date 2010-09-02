package com.revolsys.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.revolsys.collection.ThreadSharedAttributes;

public class ExecutorServiceFactory {
  private static final Object SYNC = new Object();

  private static final String KEY = ExecutorServiceFactory.class.getName()
    + ".key";

  public static ExecutorService getExecutorService() {
    synchronized (SYNC) {
      ExecutorService executorService = ThreadSharedAttributes.getAttribute(KEY);
      if (executorService == null) {
        executorService = Executors.newCachedThreadPool();
        ThreadSharedAttributes.setDefaultAttribute(KEY, executorService);
      }
      return executorService;

    }
  }

  public static void setDefaultExecutorService(
    ExecutorService executorService) {
    ThreadSharedAttributes.setDefaultAttribute(KEY, executorService);
  }

  public static void setThreadExecutorService(
    ExecutorService executorService) {
    ThreadSharedAttributes.setAttribute(KEY, executorService);
  }
}
