package com.revolsys.parallel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.revolsys.collection.map.ThreadSharedAttributes;

public class ExecutorServiceFactory {
  private static final String KEY = ExecutorServiceFactory.class.getName() + ".key";

  private static final Object SYNC = new Object();

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

  public static void setDefaultExecutorService(final ExecutorService executorService) {
    ThreadSharedAttributes.setDefaultAttribute(KEY, executorService);
  }

  public static void setThreadExecutorService(final ExecutorService executorService) {
    ThreadSharedAttributes.setAttribute(KEY, executorService);
  }
}
