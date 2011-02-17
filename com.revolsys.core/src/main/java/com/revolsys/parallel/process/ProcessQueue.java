package com.revolsys.parallel.process;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.store.Buffer;

public class ProcessQueue {
  private final Buffer<Process> processBuffer = new Buffer<Process>(200);

  private final Channel<Process> processChannel = new Channel<Process>(
    processBuffer);

  private final Set<ProcessQueueWorker> workers = Collections.synchronizedSet(new HashSet<ProcessQueueWorker>());

  private final int maxWorkers;

  private final int maxWorkerIdleTime;

  public ProcessQueue(final int maxWorkers, final int maxWorkerIdleTime) {
    this.maxWorkers = maxWorkers;
    this.maxWorkerIdleTime = maxWorkerIdleTime;
  }

  void addWorker(final ProcessQueueWorker worker) {
    synchronized (workers) {
      workers.add(worker);
    }
  }

  public synchronized void cancelProcess(final Process process) {

    if (process != null && !processBuffer.remove(process)) {
      synchronized (workers) {
        for (final ProcessQueueWorker worker : workers) {
          if (worker.getProcess() == process) {
            worker.interrupt();
          }
        }
      }
    }
  }

  public void clear() {
    processBuffer.clear();
  }

  public int getMaxWorkerIdleTime() {
    return maxWorkerIdleTime;
  }

  Channel<Process> getProcessChannel() {
    return processChannel;
  }

  void removeWorker(final ProcessQueueWorker worker) {
    synchronized (workers) {
      workers.remove(worker);
    }
  }

  public synchronized void runProcess(final Process process) {
    processChannel.write(process);
    if (workers.size() < maxWorkers && processBuffer.size() > workers.size()) {
      final ProcessQueueWorker worker = new ProcessQueueWorker(this);
      worker.start();
    }
  }

  public void stop() {
    clear();
    processChannel.close();
    synchronized (workers) {
      for (final ProcessQueueWorker worker : workers) {
        worker.interrupt();
      }
    }
    workers.clear();
  }

}
