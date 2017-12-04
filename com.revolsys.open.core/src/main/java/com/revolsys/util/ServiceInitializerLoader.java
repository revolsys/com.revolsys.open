package com.revolsys.util;

import java.util.List;
import java.util.ServiceLoader;

import com.google.common.collect.Lists;
import com.revolsys.logging.Logs;

public class ServiceInitializerLoader {
  static {
    final ServiceLoader<ServiceInitializer> serviceLoader = ServiceLoader
      .load(ServiceInitializer.class);
    final List<ServiceInitializer> services = Lists.newArrayList(serviceLoader);
    services.sort((a, b) -> Integer.compare(a.priority(), b.priority()));
    for (final ServiceInitializer serviceInitializer : services) {
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
