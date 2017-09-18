package com.revolsys.swing.parallel;

import javax.swing.SwingWorker.StateValue;

import com.revolsys.logging.Logs;

class RunnableBackgroundTask implements Runnable, BackgroundTask {

  private final Runnable runnable;

  private final String taskTitle;

  private StateValue taskStatus = StateValue.PENDING;

  private String taskThreadName;

  private long startTime = -1;

  private long endTime = -1;

  public RunnableBackgroundTask(final String taskTitle, final Runnable runnable) {
    this.taskTitle = taskTitle;
    this.runnable = runnable;
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
      this.startTime = System.currentTimeMillis();
      this.taskStatus = StateValue.STARTED;
      this.taskThreadName = Thread.currentThread().getName();
      this.runnable.run();
    } catch (final Exception e) {
      Logs.error(this, "Error processing task:" + this.taskTitle, e);
    } finally {
      this.endTime = System.currentTimeMillis();
      this.taskThreadName = null;
      this.taskStatus = StateValue.DONE;
    }
  }

}
