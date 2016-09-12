package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.revolsys.collection.map.ThreadSharedProperties;
import com.revolsys.logging.log4j.ThreadLocalAppenderRunnable;
import com.revolsys.parallel.ThreadUtil;
import com.revolsys.spring.TargetBeanProcess;

public class ProcessNetwork {

  public static void startAndWait(final Process... processes) {
    final ProcessNetwork processNetwork = new ProcessNetwork(processes);
    processNetwork.startAndWait();
  }

  public static void startAndWait(final Runnable... processes) {
    final ProcessNetwork processNetwork = new ProcessNetwork(processes);
    processNetwork.startAndWait();
  }

  private boolean autoStart;

  private int count = 0;

  private String name = "processNetwork";

  private ProcessNetwork parent;

  private final Map<Process, Thread> processes = new HashMap<>();

  private boolean running = false;

  private boolean stopping = false;

  private final Object sync = new Object();

  private ThreadGroup threadGroup;

  public ProcessNetwork() {
  }

  public ProcessNetwork(final Collection<? extends Runnable> processes) {
    for (final Runnable runnable : processes) {
      if (runnable instanceof Process) {
        final Process process = (Process)runnable;
        addProcess(process);
      } else {
        addProcess(runnable);
      }
    }
  }

  public ProcessNetwork(final Runnable... processes) {
    this(Arrays.asList(processes));
  }

  public void addProcess(final Process process) {
    synchronized (this.sync) {
      if (this.stopping) {
        return;
      } else {
        if (!this.stopping) {
          if (this.processes != null && !this.processes.containsKey(process)) {
            this.processes.put(process, null);
            process.initialize();
          }
        }
      }
    }
    if (this.parent == null) {
      if (this.running && !this.stopping) {
        start(process);
      }
    } else {
      this.parent.addProcess(process);
    }
  }

  public void addProcess(final Runnable runnable) {
    if (runnable != null) {
      final RunnableProcess process = new RunnableProcess(runnable);
      addProcess(process);
    }
  }

  public void addProcess(final String processName, final Runnable runnable) {
    if (runnable != null) {
      final RunnableProcess process = new RunnableProcess(processName, runnable);
      addProcess(process);
    }
  }

  private void finishRunning() {
    synchronized (this.sync) {
      this.running = false;
      this.processes.clear();
    }
  }

  public String getName() {
    return this.name;
  }

  public ProcessNetwork getParent() {
    return this.parent;
  }

  public Collection<Process> getProcesses() {
    if (this.processes == null) {
      return Collections.emptySet();
    } else {
      return this.processes.keySet();
    }
  }

  protected Map<Process, Thread> getProcessMap() {
    return this.processes;
  }

  protected Object getSync() {
    return this.sync;
  }

  public ThreadGroup getThreadGroup() {
    return this.threadGroup;
  }

  @PostConstruct
  public void init() {
    if (this.parent == null) {
      this.threadGroup = new ThreadGroup(this.name);
      ThreadSharedProperties.initialiseThreadGroup(this.threadGroup);
    }
  }

  public boolean isAutoStart() {
    return this.autoStart;
  }

  void removeProcess(final Process process) {
    synchronized (this.sync) {
      if (this.processes != null) {
        this.processes.remove(process);
        this.count--;
      }

      if (this.parent == null) {
        if (process.getProcessNetwork() == this) {
          process.setProcessNetwork(null);
        }
        if (this.count == 0) {
          finishRunning();
          this.sync.notifyAll();
        }
      } else {
        this.parent.removeProcess(process);
      }
    }
  }

  public void setAutoStart(final boolean autoStart) {
    this.autoStart = autoStart;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setParent(final ProcessNetwork parent) {
    this.parent = parent;
  }

  public void setProcesses(final Collection<Process> processes) {
    for (final Process process : processes) {
      addProcess(process);
    }
  }

  public void start() {
    if (this.parent == null) {
      synchronized (this.sync) {
        this.running = true;
        if (this.processes != null) {
          for (final Process process : new ArrayList<>(this.processes.keySet())) {
            process.setProcessNetwork(this);
            start(process);
          }
        }
      }
    }
  }

  private synchronized void start(final Process process) {
    if (this.parent == null) {
      if (this.processes != null) {
        Thread thread = this.processes.get(process);
        if (thread == null) {
          final Process runProcess;
          if (process instanceof TargetBeanProcess) {
            final TargetBeanProcess targetBeanProcess = (TargetBeanProcess)process;
            runProcess = targetBeanProcess.getProcess();
            this.processes.remove(process);
          } else {
            runProcess = process;
          }
          final Runnable runnable = new ProcessRunnable(this, runProcess);
          final String name = runProcess.toString();
          final Runnable appenderRunnable = new ThreadLocalAppenderRunnable(runnable);
          thread = new Thread(this.threadGroup, appenderRunnable, name);
          this.processes.put(runProcess, thread);
          if (!thread.isAlive()) {
            thread.start();
            this.count++;
          }
        }
      }
    }
  }

  public void startAndWait() {
    synchronized (this.sync) {
      start();
      waitTillFinished();
    }
  }

  @SuppressWarnings("deprecation")
  @PreDestroy
  public void stop() {
    final List<Thread> threads;
    synchronized (this.sync) {
      this.stopping = true;
      this.sync.notifyAll();
      threads = new ArrayList<>(this.processes.values());
    }
    boolean interrupted = false;
    try {
      final long maxWait = System.currentTimeMillis() + 10000;
      while (!threads.isEmpty() && System.currentTimeMillis() < maxWait) {
        for (final Iterator<Thread> threadIter = threads.iterator(); threadIter.hasNext();) {
          final Thread thread = threadIter.next();
          if (thread == null || !thread.isAlive() || Thread.currentThread() == thread) {
            threadIter.remove();
          } else {
            try {
              thread.interrupt();
            } catch (final Exception e) {
              if (e instanceof InterruptedException) {
                interrupted = true;
              }
            }
            if (!thread.isAlive()) {
              threadIter.remove();
            }
          }
        }
      }

      for (final Thread thread : threads) {
        if (thread.isAlive()) {
          try {
            thread.stop();
          } catch (final Exception e) {
            if (e instanceof InterruptedException) {
              interrupted = true;
            }
          }
        }
      }
      if (interrupted) {
        Thread.currentThread().interrupt();
      }
    } finally {
      finishRunning();
    }
  }

  @Override
  public String toString() {
    return this.name;
  }

  public void waitTillFinished() {
    if (this.parent == null) {
      synchronized (this.sync) {
        try {
          while (!this.stopping && this.count > 0) {
            ThreadUtil.pause(this.sync);
          }
        } finally {
          finishRunning();
        }
      }
    } else {
      this.parent.waitTillFinished();
    }
  }

}
