package com.revolsys.swing;

import java.util.concurrent.Callable;

import javax.swing.SwingWorker;

public class InvokeMethodSwingWorker<T, V> extends SwingWorker<T, V> {
  Callable<T> backgroundTask;

  Runnable doneTask;

  public InvokeMethodSwingWorker(final Callable<T> backgroundTask,
    final Runnable doneTask) {
    this.backgroundTask = backgroundTask;
    this.doneTask = doneTask;
  }

  @Override
  protected T doInBackground() throws Exception {
    return backgroundTask.call();
  }

  @Override
  protected void done() {
    doneTask.run();
  }
}
