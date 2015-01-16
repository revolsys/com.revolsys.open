package com.revolsys.swing.parallel;


public class RunnableSwingWorker extends AbstractSwingWorker<Void, Void> {
  private final String description;

  private final Runnable backgroundTask;

  public RunnableSwingWorker(final Runnable backgroundTask) {
    this(backgroundTask.toString(), backgroundTask);
  }

  public RunnableSwingWorker(final String description,
    final Runnable backgroundTask) {
    this.description = description;
    this.backgroundTask = backgroundTask;
  }

  @Override
  protected Void doInBackground() throws Exception {
    if (this.backgroundTask != null) {
      this.backgroundTask.run();
    }
    return null;
  }

  public String getDescription() {
    return this.description;
  }

  @Override
  public String toString() {
    return this.description;
  }

  @Override
  protected void uiTask() {
  }
}
