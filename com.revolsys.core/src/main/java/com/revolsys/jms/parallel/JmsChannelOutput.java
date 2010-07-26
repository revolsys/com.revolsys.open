package com.revolsys.jms.parallel;

import org.springframework.jms.core.JmsTemplate;

import com.revolsys.parallel.channel.AbstractChannelOutput;

public class JmsChannelOutput<T> extends AbstractChannelOutput<T> {
  private JmsTemplate jmsTemplate;

  public JmsChannelOutput(
    JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  public JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  public void setJmsTemplate(
    JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  @Override
  protected void doWrite(
    T value) {
    jmsTemplate.convertAndSend(value);
  }
}
