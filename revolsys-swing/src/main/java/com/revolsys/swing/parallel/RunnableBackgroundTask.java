package com.revolsys.swing.parallel;

class RunnableBackgroundTask extends AbstractBackgroundTask {

  private final Runnable runnable;

  public RunnableBackgroundTask(final String taskTitle, final Runnable runnable) {
    super(taskTitle);
    this.runnable = runnable;
  }

  @Override
  public void runTask() {
    this.runnable.run();
  }
}
