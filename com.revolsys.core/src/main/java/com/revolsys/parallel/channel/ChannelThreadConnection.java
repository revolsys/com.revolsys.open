package com.revolsys.parallel.channel;

import com.revolsys.util.ThreadLocalMap;

public class ChannelThreadConnection {

  private static final ThreadLocalMap<ChannelOutput<?>, ChannelThreadConnection> connections = new ThreadLocalMap<ChannelOutput<?>, ChannelThreadConnection>();

  public static void writeConnect(ChannelOutput<?> channel) {
    synchronized (connections) {
      ChannelThreadConnection connection = connections.get(channel);
      if (connection == null) {
        connection = new ChannelThreadConnection(channel);
        connections.put(channel, connection);
        channel.writeConnect();
      }
    }
  }

  private ChannelOutput<?> channel;

  private ChannelThreadConnection(ChannelOutput<?> channel) {
    this.channel = channel;
  }

  public void finalize() {
    channel.writeDisconnect();
  }
}
