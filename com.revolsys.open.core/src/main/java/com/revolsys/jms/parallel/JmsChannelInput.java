package com.revolsys.jms.parallel;

import org.springframework.jms.core.JmsTemplate;

import com.revolsys.parallel.channel.AbstractChannelInput;

public class JmsChannelInput<T> extends AbstractChannelInput<T> {
  private JmsTemplate jmsTemplate;

  public JmsChannelInput(final JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }

  @Override
  protected T doRead() {

    return doRead(JmsTemplate.RECEIVE_TIMEOUT_INDEFINITE_WAIT);
  }

  @Override
  protected T doRead(final long timeout) {
    jmsTemplate.setReceiveTimeout(timeout);
    return (T)jmsTemplate.receiveAndConvert();
  }

  public JmsTemplate getJmsTemplate() {
    return jmsTemplate;
  }

  public void setJmsTemplate(final JmsTemplate jmsTemplate) {
    this.jmsTemplate = jmsTemplate;
  }
}
