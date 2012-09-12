package com.revolsys.parallel.process;

public class ProcessRunnable implements Runnable {
  private final ProcessNetwork processManager;

  private final Process process;

  public ProcessRunnable(final ProcessNetwork processManager,
    final Process process) {
    this.processManager = processManager;
    this.process = process;
  }

  @Override
  public void run() {
    process.run();
    processManager.removeProcess(process);
  }
}
