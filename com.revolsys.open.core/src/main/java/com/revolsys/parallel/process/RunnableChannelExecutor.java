package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;

import com.revolsys.parallel.NamedThreadFactory;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.channel.ClosedException;
import com.revolsys.parallel.channel.MultiInputSelector;

public class RunnableChannelExecutor extends ThreadPoolExecutor implements
  Process, BeanNameAware {
  private String beanName;

  private List<Channel<Runnable>> channels = new ArrayList<Channel<Runnable>>();

  private final Object monitor = new Object();

  private ProcessNetwork processNetwork;

  private final AtomicInteger taskCount = new AtomicInteger();

  public RunnableChannelExecutor() {
    super(1, 100, 60, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1),
      new NamedThreadFactory());
  }

  @Override
  protected void afterExecute(final Runnable r, final Throwable t) {
    taskCount.decrementAndGet();
    synchronized (monitor) {
      monitor.notifyAll();
    }
  }

  public void closeChannels() {
    synchronized (monitor) {
      final List<Channel<Runnable>> channels = this.channels;
      if (channels != null) {
        for (final Channel<Runnable> channel : channels) {
          channel.readDisconnect();
        }
      }
      this.channels = null;
    }
  }

  @Override
  public void execute(final Runnable command) {
    if (command != null) {
      while (!isShutdown()) {
        if (taskCount.get() >= getMaximumPoolSize()) {
          synchronized (monitor) {
            try {
              monitor.wait();
            } catch (final InterruptedException e) {
              return;
            }
          }
        }
        taskCount.incrementAndGet();
        try {
          super.execute(command);
          return;
        } catch (final RejectedExecutionException e) {
          taskCount.decrementAndGet();
        } catch (final RuntimeException e) {
          taskCount.decrementAndGet();
          throw e;
        } catch (final Error e) {
          taskCount.decrementAndGet();
          throw e;
        }
      }
    }
  }

  @Override
  public String getBeanName() {
    return beanName;
  }

  public List<Channel<Runnable>> getChannels() {
    return channels;
  }

  /**
   * @return the processNetwork
   */
  @Override
  public ProcessNetwork getProcessNetwork() {
    return processNetwork;
  }

  @PostConstruct
  public void init() {
    for (final Channel<Runnable> channel : channels) {
      channel.readConnect();
    }
  }

  public void postRun() {
    closeChannels();
  }

  protected void preRun() {

  }

  @Override
  public void run() {
    preRun();
    try {
      final MultiInputSelector selector = new MultiInputSelector();

      while (!isShutdown()) {
        final List<Channel<Runnable>> channels = this.channels;
        try {
          if (!isShutdown()) {
            final Channel<Runnable> channel = selector.selectChannelInput(channels);
            if (channel != null) {
              final Runnable runnable = channel.read();
              execute(runnable);
            }
          }
        } catch (final ClosedException e) {
          final Throwable cause = e.getCause();
          if (cause instanceof InterruptedException) {
            final InterruptedException interrupedException = (InterruptedException)cause;
            throw interrupedException;
          }
          synchronized (monitor) {
            for (final Iterator<Channel<Runnable>> iterator = channels.iterator(); iterator.hasNext();) {
              final Channel<Runnable> channel = iterator.next();
              if (channel.isClosed()) {
                iterator.remove();
              }
            }
            if (channels.isEmpty()) {
              return;
            }
          }
        }
      }
    } catch (final InterruptedException e) {
    } catch (final Throwable t) {
      if (!isShutdown()) {
        LoggerFactory.getLogger(getClass()).error("Unexexpected error ", t);
      }
    } finally {
      postRun();
    }
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
    final ThreadFactory threadFactory = getThreadFactory();
    if (threadFactory instanceof NamedThreadFactory) {
      final NamedThreadFactory namedThreadFactory = (NamedThreadFactory)threadFactory;
      namedThreadFactory.setNamePrefix(beanName + "-pool");
    }
  }

  public void setChannels(final List<Channel<Runnable>> channels) {
    this.channels = channels;
  }

  /**
   * @param processNetwork the processNetwork to set
   */
  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
    if (processNetwork != null) {
      processNetwork.addProcess(this);
      final ThreadFactory threadFactory = getThreadFactory();
      if (threadFactory instanceof NamedThreadFactory) {
        final NamedThreadFactory namedThreadFactory = (NamedThreadFactory)threadFactory;
        namedThreadFactory.setParentGroup(processNetwork.getThreadGroup());
      }
    }
  }

  @PreDestroy
  public void stop() {
    shutdownNow();
    closeChannels();
    processNetwork = null;
    synchronized (monitor) {
      monitor.notifyAll();
    }
  }

  @Override
  public String toString() {
    return beanName;
  }
}
