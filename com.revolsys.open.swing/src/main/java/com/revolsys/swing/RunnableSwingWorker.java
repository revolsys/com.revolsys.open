package com.revolsys.swing;

import javax.swing.SwingWorker;

public class RunnableSwingWorker extends SwingWorker<Void, Void> {
  private final Runnable backgroundTask;

  public RunnableSwingWorker(final Runnable backgroundTask) {
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

  @Override
  public String toString() {
    return backgroundTask.toString();
  }
}
