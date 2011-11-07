package com.revolsys.orm.core.transaction;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.revolsys.parallel.channel.Channel;

public class SendToChannelAfterCommit<T> extends
  TransactionSynchronizationAdapter {
  public static <V> void send(Channel<V> channel, V value) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      SendToChannelAfterCommit<V> synchronization = new SendToChannelAfterCommit<V>(
        channel, value);
      TransactionSynchronizationManager.registerSynchronization(synchronization);
    } else {
      channel.write(value);
    }
  }

  private Channel<T> channel;

  private T object;

  public SendToChannelAfterCommit(Channel<T> channel, T object) {
    this.channel = channel;
    this.object = object;
  }

  @Override
  public void afterCommit() {
    channel.write(object);
  }
}
