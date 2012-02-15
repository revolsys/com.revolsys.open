package com.revolsys.parallel.process;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

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

  public RunnableChannelExecutor() {
    super(0, 100, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(true),
      new NamedThreadFactory());
  }

  @Override
  protected void afterExecute(final Runnable r, final Throwable t) {
    synchronized (monitor) {
      monitor.notifyAll();
    }
  }

  public String getBeanName() {
    return beanName;
  }

  public List<Channel<Runnable>> getChannels() {
    return channels;
  }

  /**
   * @return the processNetwork
   */
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
    for (final Channel<Runnable> channel : channels) {
      channel.readDisconnect();
    }
  }

  protected void preRun() {

  }

  public void run() {
    preRun();
    try {
      while (true) {
        synchronized (monitor) {
          if (getActiveCount() >= getMaximumPoolSize()) {
            try {
              monitor.wait();
            } catch (final InterruptedException e) {
            }
          }
        }
        final MultiInputSelector selector = new MultiInputSelector();
        try {
          final Channel<Runnable> channel = selector.selectChannelInput(channels);
          if (channel != null) {
            final Runnable runnable = channel.read();
            execute(runnable);
          }
        } catch (final ClosedException e) {
          boolean hasOpen = false;
          for (final Channel<Runnable> channel : channels) {
            if (!channel.isClosed()) {
              hasOpen = true;
            }
          }
          if (!hasOpen) {
            throw e;
          }
        }
      }
    } catch (final Throwable t) {
      t.printStackTrace();
    } finally {
      postRun();
    }
  }

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

  @Override
  public String toString() {
    return beanName;
  }
}
