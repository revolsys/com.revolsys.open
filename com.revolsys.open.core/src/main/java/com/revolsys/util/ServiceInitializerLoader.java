package com.revolsys.util;

import java.util.ServiceLoader;

import com.revolsys.logging.Logs;

public class ServiceInitializerLoader {
  static {
    final ServiceLoader<ServiceInitializer> serviceLoader = ServiceLoader
      .load(ServiceInitializer.class);
    for (final ServiceInitializer serviceInitializer : serviceLoader) {
      final long startTime = System.currentTimeMillis();
      try {
        serviceInitializer.initializeService();
      } catch (final Throwable e) {
        Logs.error(serviceInitializer, "Unable to initialize", e);
      }
      Dates.debugEllapsedTime(ServiceInitializer.class, "init\t" + serviceInitializer.getClass(),
        startTime);
    }
  }

  static void initializeServices() {
  }
}
