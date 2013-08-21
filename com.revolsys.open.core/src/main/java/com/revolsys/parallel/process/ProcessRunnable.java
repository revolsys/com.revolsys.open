package com.revolsys.parallel.process;

import com.revolsys.parallel.AbstractRunnable;

public class ProcessRunnable extends AbstractRunnable {
  private final ProcessNetwork processManager;

  private final Process process;

  public ProcessRunnable(final ProcessNetwork processManager,
    final Process process) {
    this.processManager = processManager;
    this.process = process;
  }

  @Override
  public void doRun() {
    try {
      process.run();
    } finally {
      processManager.removeProcess(process);
    }
  }
}
