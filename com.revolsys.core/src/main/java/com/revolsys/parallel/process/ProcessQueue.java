package com.revolsys.parallel.process;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.revolsys.parallel.channel.Buffer;
import com.revolsys.parallel.channel.Channel;

public class ProcessQueue {
  private Buffer<Process> processBuffer = new Buffer<Process>(200);

  private Channel<Process> processChannel = new Channel<Process>(processBuffer);

  private Set<ProcessQueueWorker> workers = Collections.synchronizedSet(new HashSet<ProcessQueueWorker>());

  private int maxWorkers;

  private int maxWorkerIdleTime;

  public ProcessQueue(int maxWorkers, int maxWorkerIdleTime) {
    this.maxWorkers = maxWorkers;
    this.maxWorkerIdleTime = maxWorkerIdleTime;
  }

  public synchronized void cancelProcess(final Process process) {

    if (process != null && !processBuffer.remove(process)) {
      synchronized (workers) {
        for (ProcessQueueWorker worker : workers) {
          if (worker.getProcess() == process) {
            worker.interrupt();
          }
        }
      }
    }
  }

  public synchronized void runProcess(final Process process) {
    processChannel.write(process);
    if (workers.size() < maxWorkers && processBuffer.size() > workers.size()) {
      ProcessQueueWorker worker = new ProcessQueueWorker(this);
      worker.start();
    }
  }

  public void clear() {
    processBuffer.clear();
  }

  public void stop() {
    clear();
    processChannel.close();
    synchronized (workers) {
      for (ProcessQueueWorker worker : workers) {
        worker.interrupt();
      }
    }
    workers.clear();
  }

  public int getMaxWorkerIdleTime() {
    return maxWorkerIdleTime;
  }

  Channel<Process> getProcessChannel() {
    return processChannel;
  }

  void addWorker(final ProcessQueueWorker worker) {
    synchronized (workers) {
      workers.add(worker);
    }
  }

  void removeWorker(final ProcessQueueWorker worker) {
    synchronized (workers) {
      workers.remove(worker);
    }
  }

}
