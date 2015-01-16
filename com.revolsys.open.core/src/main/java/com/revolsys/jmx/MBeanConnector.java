package com.revolsys.jmx;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

public class MBeanConnector {
  MBeanServerConnection connection;

  JMXConnector jmxConnector;

  public void close() {
    try {
      this.jmxConnector.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public void connect(final String connectionUrl) {
    this.jmxConnector = JmxUtil.getJmxConnector(connectionUrl);
    this.connection = JmxUtil.getMbeanServerConnection(this.jmxConnector);
  }

  public MBeanServerConnection getConnection() {
    return this.connection;
  }

  public JMXConnector getJmxConnector() {
    return this.jmxConnector;
  }

  public void setConnection(final MBeanServerConnection connection) {
    this.connection = connection;
  }

  public void setJmxConnector(final JMXConnector jmxConnector) {
    this.jmxConnector = jmxConnector;
  }

}
