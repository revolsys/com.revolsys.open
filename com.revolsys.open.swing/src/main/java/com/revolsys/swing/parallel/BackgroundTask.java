package com.revolsys.swing.parallel;

import javax.swing.SwingWorker.StateValue;

import com.revolsys.swing.menu.MenuFactory;

public interface BackgroundTask {

  static BackgroundTask runnable(final String taskTitle, final Runnable runnable) {
    final RunnableBackgroundTask task = new RunnableBackgroundTask(taskTitle, runnable);
    task.start();
    return task;
  }

  default MenuFactory getMenu() {
    return null;
  }

  default String getTaskMessage() {
    return null;
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
