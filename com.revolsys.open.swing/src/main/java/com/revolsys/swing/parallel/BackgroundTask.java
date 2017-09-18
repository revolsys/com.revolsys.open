package com.revolsys.swing.parallel;

import javax.swing.SwingWorker.StateValue;

public interface BackgroundTask {

  static void runnable(final String taskTitle, final Runnable runnable) {
    final RunnableBackgroundTask task = new RunnableBackgroundTask(taskTitle, runnable);
    BackgroundTaskManager.addTask(task);
    final Thread thread = new Thread(task);
    thread.start();
  }

  StateValue getTaskStatus();

  String getTaskThreadName();

  default long getTaskTime() {
    return -1;
  }

  String getTaskTitle();

  default boolean isTaskClosed() {
    return false;
  }
}
