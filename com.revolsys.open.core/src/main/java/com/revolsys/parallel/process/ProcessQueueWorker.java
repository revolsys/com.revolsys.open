package com.revolsys.parallel.process;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public class ProcessQueueWorker extends Thread {
  private final Channel<Process> in;

  private Process process;

  private final ProcessQueue queue;

  public ProcessQueueWorker(final ProcessQueue queue) {
    this.queue = queue;
    this.in = queue.getProcessChannel();
    setDaemon(true);
  }

  public String getBeanName() {
    return getClass().getName();
  }

  public Process getProcess() {
    return this.process;
  }

  @Override
  public void run() {
    this.queue.addWorker(this);
    try {
      while (true) {
        this.process = this.in.read(this.queue.getMaxWorkerIdleTime());
        if (this.process == null) {
          return;
        } else {
          try {
            this.process.run();
          } catch (final Exception e) {
            if (e instanceof ThreadInterruptedException) {
              throw (ThreadInterruptedException)e;
            } else {
              final Class<? extends Process> processClass = this.process.getClass();
              final Logger log = LoggerFactory.getLogger(processClass);
              log.error(e.getMessage(), e);
            }
          }
        }
        this.process = null;
      }
    } catch (final ClosedException e) {
      return;
    } finally {
      this.queue.removeWorker(this);
    }
  }

  public void setBeanName(final String name) {
  }
}
