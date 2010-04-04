package com.revolsys.parallel.process;

import org.springframework.beans.factory.InitializingBean;

import com.revolsys.parallel.channel.Channel;

public class ProcessLink<T> implements InitializingBean {

  private OutProcess<T> source;

  private InProcess<T> target;

  public OutProcess<T> getSource() {
    return source;
  }

  public void setSource(
    final OutProcess<T> source) {
    this.source = source;
  }

  public InProcess<T> getTarget() {
    return target;
  }

  public void setTarget(
    final InProcess<T> target) {
    this.target = target;
  }

  public void afterPropertiesSet()
    throws Exception {
    final Channel<T> channel = target.getIn();
    source.setOut(channel);

  }
}
