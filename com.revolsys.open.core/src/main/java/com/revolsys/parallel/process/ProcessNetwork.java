package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import com.revolsys.collection.ThreadSharedAttributes;
import com.revolsys.logging.log4j.ThreadLocalAppenderRunnable;
import com.revolsys.spring.TargetBeanFactoryBean;
import com.revolsys.spring.TargetBeanProcess;

public class ProcessNetwork implements BeanPostProcessor,
  ApplicationListener<ContextRefreshedEvent> {

  private int count = 0;

  private Map<Process, Thread> processes = new HashMap<Process, Thread>();

  boolean running = false;

  private ThreadGroup threadGroup;

  private boolean autoStart;

  private String name = "processNetwork";

  private ProcessNetwork parent;

  private final Object sync = new Object();

  public ProcessNetwork() {
  }

  public void addProcess(final Process process) {
    synchronized (sync) {
      if (processes != null && !processes.containsKey(process)) {
        processes.put(process, null);
      }
    }
    if (parent == null) {
      if (running) {
        start(process);
      }
    } else {
      parent.addProcess(process);
    }
  }

  private void finishRunning() {
    running = false;
    processes = null;
  }

  public String getName() {
    return name;
  }

  public ProcessNetwork getParent() {
    return parent;
  }

  public Collection<Process> getProcesses() {
    if (processes == null) {
      return Collections.emptySet();
    } else {
      return processes.keySet();
    }
  }

  public ThreadGroup getThreadGroup() {
    return threadGroup;
  }

  @PostConstruct
  public void init() {
    if (parent == null) {
      threadGroup = new ThreadGroup(name);
      ThreadSharedAttributes.initialiseThreadGroup(threadGroup);
    }
  }

  public boolean isAutoStart() {
    return autoStart;
  }

  public void onApplicationEvent(final ContextRefreshedEvent event) {
    if (autoStart) {
      start();
    }
  }

  public Object postProcessAfterInitialization(
    final Object bean,
    final String beanName) throws BeansException {
    if (parent == null) {
      if (bean instanceof TargetBeanFactoryBean) {
        final TargetBeanFactoryBean targetBean = (TargetBeanFactoryBean)bean;
        final Class<?> targetClass = targetBean.getObjectType();
        if (Process.class.isAssignableFrom(targetClass)) {
          try {
            final Process process = new TargetBeanProcess(targetBean);
            addProcess(process);
          } catch (final Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }

        }
      } else if (bean instanceof Process) {
        final Process process = (Process)bean;
        // Check to see if this was a target bean, if so make sure duplicate
        // threads aren't created
        if (processes != null) {
          for (final Entry<Process, Thread> entry : processes.entrySet()) {
            final Process otherProcess = entry.getKey();
            if (otherProcess instanceof TargetBeanProcess) {
              final TargetBeanProcess targetProcessBean = (TargetBeanProcess)otherProcess;
              if (targetProcessBean.isInstanceCreated()) {
                final Process targetProcess = targetProcessBean.getProcess();
                if (targetProcess == process) {
                  synchronized (sync) {
                    final Thread thread = entry.getValue();
                    processes.put(targetProcess, thread);
                    processes.remove(otherProcess);
                    return bean;
                  }
                }
              }
            }
          }
        }
        addProcess(process);
      }
    } else {
      parent.postProcessAfterInitialization(bean, beanName);
    }
    return bean;
  }

  public Object postProcessBeforeInitialization(
    final Object bean,
    final String beanName) throws BeansException {
    return bean;
  }

  void removeProcess(final Process process) {
    synchronized (sync) {
      if (processes != null) {
        processes.remove(process);
        count--;
      }

      if (parent == null) {
        if (process.getProcessNetwork() == this) {
          process.setProcessNetwork(null);
        }
        if (count == 0) {
          finishRunning();
          sync.notifyAll();
        }
      } else {
        parent.removeProcess(process);
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
    if (parent == null) {
      synchronized (sync) {
        running = true;
        if (processes != null) {
          for (final Process process : new ArrayList<Process>(
            processes.keySet())) {
            process.setProcessNetwork(this);
            start(process);
          }
        }
      }
    }
  }

  private synchronized void start(final Process process) {
    if (parent == null) {
      if (processes != null) {
        Thread thread = processes.get(process);
        if (thread == null) {
          final Process runProcess;
          if (process instanceof TargetBeanProcess) {
            final TargetBeanProcess targetBeanProcess = (TargetBeanProcess)process;
            runProcess = targetBeanProcess.getProcess();
            processes.remove(process);
          } else {
            runProcess = process;
          }
          final Runnable runnable = new ProcessRunnable(this, runProcess);
          final String name = runProcess.toString();
          final Runnable appenderRunnable = new ThreadLocalAppenderRunnable(
            runnable);
          thread = new Thread(threadGroup, appenderRunnable, name);
          processes.put(runProcess, thread);
          if (!thread.isAlive()) {
            thread.start();
            count++;
          }
        }
      }
    }
  }

  public void startAndWait() {
    synchronized (sync) {
      start();
      waitTillFinished();
    }
  }

  @PreDestroy
  public void stop() {
    synchronized (sync) {
      if (processes != null) {
        try {
          for (final Thread thread : new ArrayList<Thread>(processes.values())) {
            if (thread != null && Thread.currentThread() != thread
              && thread.isAlive()) {
              thread.interrupt();
            }
          }
        } finally {
          finishRunning();
        }
      }
      sync.notifyAll();
    }
  }

  @Override
  public String toString() {
    return name;
  }

  public void waitTillFinished() {
    if (parent == null) {

      synchronized (sync) {
        try {
          while (count > 0) {
            try {
              sync.wait();
            } catch (final InterruptedException e) {
              return;
            }
          }
        } finally {
          finishRunning();
        }
      }
    } else {
      parent.waitTillFinished();
    }
  }

}
