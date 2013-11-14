package com.revolsys.parallel.process;

import org.apache.log4j.Logger;

import com.revolsys.parallel.ThreadInterruptedException;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;

public class ProcessQueueWorker extends Thread {
  private final ProcessQueue queue;

  private final Channel<Process> in;

  private Process process;

  public ProcessQueueWorker(final ProcessQueue queue) {
    this.queue = queue;
    this.in = queue.getProcessChannel();
    setDaemon(true);
  }

  public String getBeanName() {
    return getClass().getName();
  }

  public Process getProcess() {
    return process;
  }

  @Override
  public void run() {
    queue.addWorker(this);
    try {
      while (true) {
        process = in.read(queue.getMaxWorkerIdleTime());
        if (process == null) {
          return;
        } else {
          try {
            process.run();
          } catch (final Exception e) {
            if (e instanceof ThreadInterruptedException) {
              throw (ThreadInterruptedException)e;
            } else {
              final Class<? extends Process> processClass = process.getClass();
              final Logger log = Logger.getLogger(processClass);
              log.error(e.getMessage(), e);
            }
          }
        }
        process = null;
      }
    } catch (final ClosedException e) {
      return;
    } finally {
      queue.removeWorker(this);
    }
  }

  public void setBeanName(final String name) {
  }
}
