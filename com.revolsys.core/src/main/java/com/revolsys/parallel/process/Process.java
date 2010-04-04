package com.revolsys.parallel.process;

import org.springframework.beans.factory.BeanNameAware;

public interface Process extends Runnable, BeanNameAware {
  String getBeanName();
}
