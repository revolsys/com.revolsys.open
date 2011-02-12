package com.revolsys.parallel.process;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;

public abstract class AbstractResetableProcess extends AbstractProcess {
  private String status = "initialized";

  private boolean running = false;

  private boolean pause = false;

  private boolean reset = false;

  private boolean waitForExecutionToFinish = false;

  private final Set<UUID> executions = new LinkedHashSet<UUID>();

  private long waitTime = 1000;

  public AbstractResetableProcess() {
  }

  public AbstractResetableProcess(final long waitTime) {
    this.waitTime = waitTime;
  }

  protected abstract boolean execute();

  protected void finishExecution(final UUID id) {
    synchronized (executions) {
      executions.remove(id);
      executions.notifyAll();
    }
  }

  @ManagedAttribute
  public int getExecutionCount() {
    return executions.size();
  }

  @ManagedAttribute
  public String getStatus() {
    return status;
  }

  public long getWaitTime() {
    return waitTime;
  }

  /**
   * The hard reset causes the scheduler loop to restart ignoring all current
   * executing requests. Upon reset the counts of executing requests and the
   * status of all jobs will be updated to ensure consistency.
   */
  @ManagedOperation
  public void hardReset() {
    waitForExecutionToFinish = false;
    pause = false;
    reset = true;
  }

  /**
   * The pause causes the scheduler to sleep until a soft or hard reset is
   * initiated.
   */
  @ManagedOperation
  public void pause() {
    pause = true;
  }

  protected void postRun() {
  }

  protected void preRun() {
  }

  protected void reset() {
  }

  public void run() {
    preRun();
    running = true;
    try {
      while (running) {
        status = "resetting";
        executions.clear();
        reset();
        reset = false;
        while (running && !reset) {
          status = "starting execution";
          if (pause || !execute()) {
            if (pause) {
              status = "paused";
            } else {
              status = "waiting";
            }
            synchronized (this) {
              try {
                wait(waitTime);
              } catch (final InterruptedException e) {
              }
            }
          }
        }

        synchronized (executions) {
          while (waitForExecutionToFinish && !executions.isEmpty()) {
            waitOnExecutions();
          }
        }
      }
    } finally {
      try {
        postRun();
      } finally {
        running = false;
        status = "terminated";
      }
    }
  }

  protected void setStatus(final String status) {
    this.status = status;
  }

  public void setWaitTime(final long waitTime) {
    this.waitTime = waitTime;
  }

  /**
   * The soft reset causes the scheduler loop to restart after all current
   * executing requests have completed. Upon reset the counts of executing
   * requests and the status of all jobs will be updated to ensure consistency.
   */
  @ManagedOperation
  public void softReset() {
    waitForExecutionToFinish = true;
    pause = false;
    reset = true;
  }

  protected UUID startExecution() {
    synchronized (executions) {
      final UUID id = UUID.randomUUID();
      executions.add(id);
      executions.notifyAll();
      return id;
    }
  }

  protected void waitOnExecutions() {
    synchronized (executions) {
      status = "waiting on executions";
      try {
        executions.wait(waitTime);
      } catch (final InterruptedException e) {
      }
    }
  }

}
