package com.revolsys.parallel.process;

import org.springframework.beans.factory.BeanNameAware;

public abstract class AbstractProcess implements Process, BeanNameAware {
  private String beanName = getClass().getName();

  private ProcessNetwork processNetwork;

  @Override
  public String getBeanName() {
    return beanName;
  }

  /**
   * @return the processNetwork
   */
  @Override
  public ProcessNetwork getProcessNetwork() {
    return processNetwork;
  }

  @Override
  public void setBeanName(final String beanName) {
    this.beanName = beanName;
  }

  /**
   * @param processNetwork the processNetwork to set
   */
  @Override
  public void setProcessNetwork(final ProcessNetwork processNetwork) {
    this.processNetwork = processNetwork;
    if (processNetwork != null) {
      processNetwork.addProcess(this);
    }
  }

  public void stop() {
  }

  @Override
  public String toString() {
    final String className = getClass().getName();
    if (beanName == null) {
      return className;
    } else {
      return beanName + " (" + className + ")";
    }
  }
}
