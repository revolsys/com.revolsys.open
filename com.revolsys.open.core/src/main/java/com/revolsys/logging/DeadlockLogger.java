package com.revolsys.logging;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class DeadlockLogger implements Runnable {
  private static final int WAIT_TIME = 60000;

  private static Thread thread;

  public static synchronized void initialize() {
    if (thread == null || !thread.isAlive()) {
      final DeadlockLogger deadlockLogger = new DeadlockLogger();
      final Thread thread = new Thread(deadlockLogger, "Deadlock-detection");
      thread.setDaemon(true);
      thread.start();
      DeadlockLogger.thread = thread;
    }
  }

  @Override
  public void run() {
    final ThreadMXBean mbean = ManagementFactory.getThreadMXBean();

    while (true) {
      synchronized (this) {
        try {
          wait(WAIT_TIME);
        } catch (final InterruptedException e) {
          return;
        }
        final long[] deadlockedThreadIds = mbean.findDeadlockedThreads();

        if (deadlockedThreadIds != null) {
          final ThreadInfo[] threadInfos1 = mbean.getThreadInfo(deadlockedThreadIds, true, true);
          final ThreadInfo[] threadInfos2 = mbean.getThreadInfo(deadlockedThreadIds, 500);
          final StringBuilder message = new StringBuilder("Deadlock detected\n\n");
          for (int i = 0; i < threadInfos1.length; i++) {
            final ThreadInfo threadInfo1 = threadInfos1[i];
            final ThreadInfo threadInfo2 = threadInfos2[i];
            message.append(threadInfo1);
            message.append(threadInfo2);
          }
          Logs.error(this, message.toString());
          // Quit thread now point running if there is a deadlock
          return;
        }

      }
    }
  }
}
