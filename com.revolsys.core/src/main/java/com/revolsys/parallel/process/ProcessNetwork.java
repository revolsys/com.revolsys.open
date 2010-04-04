package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import com.revolsys.logging.log4j.ThreadLocalAppenderRunnable;
import com.revolsys.parallel.tools.ThreadSharedAttributes;

public class ProcessNetwork implements BeanPostProcessor {
  private Map<Process, Thread> processes = new HashMap<Process, Thread>();

  private ThreadGroup threadGroup = new ThreadGroup("Processes");

  private int count = 0;

  boolean running = false;

  public ProcessNetwork() {
    ThreadSharedAttributes.initialiseThreadGroup(threadGroup);
  }

  public synchronized void addProcess(
    Process process) {
    if (!processes.containsKey(process)) {
      Runnable runnable = new ProcessRunnable(this, process);
      String name = process.toString();
      Runnable appenderRunnable = new ThreadLocalAppenderRunnable(runnable);
      Thread thread = new Thread(threadGroup, appenderRunnable, name);
      processes.put(process, thread);
      if (running) {
        startProcess(thread);
      }
    }
  }

  synchronized void removeProcess(
    Process process) {
    processes.remove(process);
    count--;
    if (count == 0) {
      finishRunning();
      notify();
    }
  }

  private void finishRunning() {
    running = false;
  }

  public Collection<Process> getProcesses() {
    return processes.keySet();
  }

  public void setProcesses(
    final Collection<Process> processes) {
    for (Process process : processes) {
      addProcess(process);
    }
  }

  public synchronized void startAndWait() {
    start();
    waitTillFinished();
  }

  public synchronized void waitTillFinished() {
    while (count > 0) {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
    finishRunning();
  }

  public synchronized void start() {
    running = true;
    for (Thread thread : processes.values()) {
      startProcess(thread);
    }
  }

  private void startProcess(
    Thread thread) {
    if (!thread.isAlive()) {
      thread.start();
      count++;
    }
  }

  @SuppressWarnings("deprecation")
  @PreDestroy
  public void stop() {
    ArrayList<Thread> processesToStop = new ArrayList<Thread>(
      processes.values());
    for (Thread thread : processesToStop) {
      if (thread.isAlive()) {
        thread.stop();
      }
    }
    finishRunning();
  }

  public Object postProcessAfterInitialization(
    final Object bean,
    final String beanName)
    throws BeansException {
    if (bean instanceof Process) {
      Process process = (Process)bean;
      addProcess(process);
    }
    return bean;
  }

  public Object postProcessBeforeInitialization(
    final Object bean,
    final String beanName)
    throws BeansException {
    return bean;
  }
}
