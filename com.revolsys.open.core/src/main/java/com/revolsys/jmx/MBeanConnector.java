package com.revolsys.jmx;

import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;

public class MBeanConnector {
  MBeanServerConnection connection;

  JMXConnector jmxConnector;

  public void close() {
    try {
      jmxConnector.close();
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  public void connect(
    final String connectionUrl) {
    jmxConnector = JmxUtil.getJmxConnector(connectionUrl);
    connection = JmxUtil.getMbeanServerConnection(jmxConnector);
  }

  public MBeanServerConnection getConnection() {
    return connection;
  }

  public JMXConnector getJmxConnector() {
    return jmxConnector;
  }

  public void setConnection(
    final MBeanServerConnection connection) {
    this.connection = connection;
  }

  public void setJmxConnector(
    final JMXConnector jmxConnector) {
    this.jmxConnector = jmxConnector;
  }

}
