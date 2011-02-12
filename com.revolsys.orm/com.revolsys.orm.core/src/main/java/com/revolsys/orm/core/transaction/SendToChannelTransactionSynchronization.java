package com.revolsys.orm.core.transaction;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;

import com.revolsys.parallel.channel.Channel;

public class SendToChannelTransactionSynchronization<T> extends
  TransactionSynchronizationAdapter {
  private Channel<T> channel;

  private T object;

  public SendToChannelTransactionSynchronization(Channel<T> channel, T object) {
    this.channel = channel;
    this.object = object;
  }

  @Override
  public void afterCommit() {
    channel.write(object);
  }
}
