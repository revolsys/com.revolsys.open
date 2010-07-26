package com.revolsys.jms.parallel;

import org.springframework.jms.core.JmsTemplate;

import com.revolsys.parallel.channel.AbstractChannelInput;

public class JmsChannelInput<T> extends AbstractChannelInput<T> {
  private JmsTemplate jmsTemplate;

  public JmsChannelInput(
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
  protected T doRead() {

    return (T)doRead(JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT);
  }

  @Override
  protected T doRead(
    long timeout) {
    jmsTemplate.setReceiveTimeout(timeout);
    return (T)jmsTemplate.receiveAndConvert();
  }
}
