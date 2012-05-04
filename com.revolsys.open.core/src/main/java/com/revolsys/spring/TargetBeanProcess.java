package com.revolsys.spring;

import org.springframework.beans.factory.BeanFactory;

import com.revolsys.parallel.process.Process;
import com.revolsys.parallel.process.ProcessNetwork;

public class TargetBeanProcess implements Process {
  private final TargetBeanFactoryBean bean;

  private String beanName;

  private ProcessNetwork processNetwork;

  public TargetBeanProcess(final TargetBeanFactoryBean bean) {
    this.bean = bean;
  }

  public String getBeanName() {
    return beanName;
  }

  public Process getProcess() {
    try {
      return (Process)bean.getObject();
    } catch (final Exception e) {
      throw new RuntimeException("Unable to get process bean ", e);
    }
  }

  public ProcessNetwork getProcessNetwork() {
    return processNetwork;
  }

  public BeanFactory getTargetBeanFactory() {
    return bean.getTargetBeanFactory();
  }

  public boolean isInstanceCreated() {
    return bean.isInstanceCreated();
  }

  public void run() {
  }

  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
  }

  @Override
  public String toString() {
    if (bean == null) {
      return "Target=" + beanName;
    } else {
      return bean.toString();
    }
  }
}
