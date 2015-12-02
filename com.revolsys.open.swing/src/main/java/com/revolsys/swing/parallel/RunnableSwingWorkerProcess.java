package com.revolsys.swing.parallel;

public class RunnableSwingWorkerProcess extends AbstractSwingWorker<Void, Void> {

  private final RunnableSwingWorkerManager manager;

  public RunnableSwingWorkerProcess(final RunnableSwingWorkerManager manager) {
    this.manager = manager;
  }

  @Override
  protected Void handleBackground() {
    do {
      final Runnable task = this.manager.getNextTask();
      if (task == null) {
        return null;
      } else {
        task.run();
      }
    } while (true);
  }

  @Override
  public String toString() {
    return this.manager.toString();
  }
}
