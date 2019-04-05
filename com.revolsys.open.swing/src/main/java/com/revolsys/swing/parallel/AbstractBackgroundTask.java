package com.revolsys.swing.parallel;

import javax.swing.SwingWorker.StateValue;

import org.jeometry.common.logging.Logs;

import com.revolsys.swing.menu.MenuFactory;

public abstract class AbstractBackgroundTask implements Runnable, BackgroundTask {

  protected final String taskTitle;

  private StateValue taskStatus = StateValue.PENDING;

  private String taskThreadName;

  private long startTime = -1;

  private long endTime = -1;

  private final MenuFactory menu = new MenuFactory();

  public AbstractBackgroundTask(final String taskTitle) {
    this.taskTitle = taskTitle;
  }

  @Override
  public MenuFactory getMenu() {
    return this.menu;
  }

  @Override
  public StateValue getTaskStatus() {
    return this.taskStatus;
  }

  @Override
  public String getTaskThreadName() {
    return this.taskThreadName;
  }

  @Override
  public long getTaskTime() {
    if (this.startTime == -1) {
      return -1;
    } else if (this.endTime == -1) {
      return System.currentTimeMillis() - this.startTime;
    } else {
      return this.endTime - this.startTime;
    }
  }

  @Override
  public String getTaskTitle() {
    return this.taskTitle;
  }

  @Override
  public void run() {
    try {
      this.taskThreadName = Thread.currentThread().getName();
      this.startTime = System.currentTimeMillis();
      this.taskStatus = StateValue.STARTED;
      BackgroundTaskManager.taskStatusChanged();
      runTask();
    } catch (final Exception e) {
      Logs.error(this, "Error processing task:" + this.taskTitle, e);
    } finally {
      this.endTime = System.currentTimeMillis();
      this.taskThreadName = null;
      this.taskStatus = StateValue.DONE;
      BackgroundTaskManager.taskStatusChanged();
    }
  }

  protected abstract void runTask();

  public void start() {
    BackgroundTaskManager.addTask(this);
    final Thread thread = new Thread(this);
    thread.start();
  }
}
