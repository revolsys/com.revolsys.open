package com.revolsys.parallel.process;

import org.springframework.beans.factory.BeanNameAware;

public interface Process extends Runnable, BeanNameAware {
  String getBeanName();

  ProcessNetwork getProcessNetwork();

  default void initialize() {
  }

  void setProcessNetwork(final ProcessNetwork processNetwork);
}
