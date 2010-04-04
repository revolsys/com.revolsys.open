package com.revolsys.parallel.process;

public class ProcessRunnable implements Runnable {
  private ProcessNetwork processManager;

  private Process process;

  public ProcessRunnable(ProcessNetwork processManager,
    Process process) {
    this.processManager = processManager;
    this.process = process;
  }

  public void run() {
    process.run();
    processManager.removeProcess(process);
  }
}
