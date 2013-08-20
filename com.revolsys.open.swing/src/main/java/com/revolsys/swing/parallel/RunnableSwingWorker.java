package com.revolsys.swing.parallel;

import javax.swing.SwingWorker;

public class RunnableSwingWorker extends SwingWorker<Void, Void> {
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
    if (backgroundTask != null) {
      backgroundTask.run();
    }
    return null;
  }

  @Override
  protected void done() {

  }

  public String getDescription() {
    return description;
  }

  @Override
  public String toString() {
    return description;
  }
}
