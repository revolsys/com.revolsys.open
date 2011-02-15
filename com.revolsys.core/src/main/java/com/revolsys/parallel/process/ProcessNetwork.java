package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PreDestroy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.logging.log4j.ThreadLocalAppenderRunnable;

public class ProcessNetwork implements BeanPostProcessor,
  ApplicationListener<ContextRefreshedEvent> {

  private int count = 0;

  private final Map<Process, Thread> processes = new HashMap<Process, Thread>();

  boolean running = false;

  private final ThreadGroup threadGroup = new ThreadGroup("Processes");

  private boolean autoStart;

  public ProcessNetwork() {
    ThreadSharedAttributes.initialiseThreadGroup(threadGroup);
  }

  public void addProcess(final Process process) {
    synchronized (processes) {
      if (!processes.containsKey(process)) {
        final Runnable runnable = new ProcessRunnable(this, process);
        final String name = process.toString();
        final Runnable appenderRunnable = new ThreadLocalAppenderRunnable(
          runnable);
        final Thread thread = new Thread(threadGroup, appenderRunnable, name);
        processes.put(process, thread);
        if (running) {
          startProcess(thread);
        }
      }
    }
  }

  private void finishRunning() {
    running = false;
  }

  public Collection<Process> getProcesses() {
    return processes.keySet();
  }

  public Object postProcessAfterInitialization(final Object bean,
    final String beanName) throws BeansException {
    if (bean instanceof Process) {
      final Process process = (Process)bean;
      addProcess(process);
    }
    return bean;
  }

  public Object postProcessBeforeInitialization(final Object bean,
    final String beanName) throws BeansException {
    return bean;
  }

  void removeProcess(final Process process) {
    synchronized (processes) {
      if (process instanceof AbstractProcess) {
        AbstractProcess proc = (AbstractProcess)process;
        proc.setProcessNetwork(null);
      }
      processes.remove(process);
      count--;
      if (count == 0) {
        finishRunning();
        processes.notify();
      }
    }
  }

  public void setProcesses(final Collection<Process> processes) {
    for (final Process process : processes) {
      addProcess(process);
    }
  }

  public void start() {
    synchronized (processes) {
      running = true;
      for (final Entry<Process, Thread> entry : processes.entrySet()) {
        Process process = entry.getKey();
        if (process instanceof AbstractProcess) {
          AbstractProcess proc = (AbstractProcess)process;
          proc.setProcessNetwork(this);
        }
        Thread thread = entry.getValue();
        startProcess(thread);
      }
    }
  }

  public void startAndWait() {
    synchronized (processes) {
      start();
      waitTillFinished();
    }
  }

  private void startProcess(final Thread thread) {
    if (!thread.isAlive()) {
      thread.start();
      count++;
    }
  }

  @SuppressWarnings("deprecation")
  @PreDestroy
  public void stop() {
    synchronized (processes) {
      final List<Thread> processesToStop = new ArrayList<Thread>(
        processes.values());
      try {
        for (final Thread thread : processesToStop) {
          if (Thread.currentThread() != thread && thread.isAlive()) {
            thread.stop();
          }
        }
      } finally {
        processes.notify();
        finishRunning();
      }
    }
  }

  public void waitTillFinished() {
    synchronized (processes) {
      while (count > 0) {
        try {
          processes.wait();
        } catch (final InterruptedException e) {
        }
      }
      finishRunning();
    }
  }

  public void onApplicationEvent(ContextRefreshedEvent event) {
    if (autoStart) {
      start();
    }
  }

  public boolean isAutoStart() {
    return autoStart;
  }

  public void setAutoStart(boolean autoStart) {
    this.autoStart = autoStart;
  }
}
