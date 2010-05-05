package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PreDestroy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;
import org.springframework.core.PriorityOrdered;

import com.revolsys.logging.log4j.ThreadLocalAppenderRunnable;
import com.revolsys.parallel.tools.ThreadSharedAttributes;

public class ProcessNetwork implements BeanPostProcessor {
  private int count = 0;

  private final Map<Process, Thread> processes = new HashMap<Process, Thread>();

  boolean running = false;

  private final ThreadGroup threadGroup = new ThreadGroup("Processes");

  public ProcessNetwork() {
    ThreadSharedAttributes.initialiseThreadGroup(threadGroup);
  }

  public synchronized void addProcess(
    final Process process) {
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

  private void finishRunning() {
    running = false;
  }

  public Collection<Process> getProcesses() {
    return processes.keySet();
  }

  public Object postProcessAfterInitialization(
    final Object bean,
    final String beanName)
    throws BeansException {
    if (bean instanceof Process) {
      final Process process = (Process)bean;
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

  synchronized void removeProcess(
    final Process process) {
    processes.remove(process);
    count--;
    if (count == 0) {
      finishRunning();
      notify();
    }
  }

  public void setProcesses(
    final Collection<Process> processes) {
    for (final Process process : processes) {
      addProcess(process);
    }
  }

  public synchronized void start() {
    running = true;
    for (final Thread thread : processes.values()) {
      startProcess(thread);
    }
  }

  public synchronized void startAndWait() {
    start();
    waitTillFinished();
  }

  private void startProcess(
    final Thread thread) {
    if (!thread.isAlive()) {
      thread.start();
      count++;
    }
  }

  @SuppressWarnings("deprecation")
  @PreDestroy
  public void stop() {
    final ArrayList<Thread> processesToStop = new ArrayList<Thread>(
      processes.values());
    for (final Thread thread : processesToStop) {
      if (thread.isAlive()) {
        thread.stop();
      }
    }
    finishRunning();
  }

  public synchronized void waitTillFinished() {
    while (count > 0) {
      try {
        wait();
      } catch (final InterruptedException e) {
      }
    }
    finishRunning();
  }
}
