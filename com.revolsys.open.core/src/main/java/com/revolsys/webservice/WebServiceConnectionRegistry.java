package com.revolsys.webservice;

import java.io.File;
import java.util.Map;

import org.slf4j.LoggerFactory;

import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.connection.AbstractConnectionRegistry;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.Property;

public class WebServiceConnectionRegistry extends AbstractConnectionRegistry<WebServiceConnection> {
  private static final ThreadLocal<WebServiceConnectionRegistry> threadRegistry = new ThreadLocal<WebServiceConnectionRegistry>();

  public static WebServiceConnectionRegistry getForThread() {
    return WebServiceConnectionRegistry.threadRegistry.get();
  }

  public static WebServiceConnectionRegistry setForThread(
    final WebServiceConnectionRegistry registry) {
    final WebServiceConnectionRegistry oldValue = getForThread();
    WebServiceConnectionRegistry.threadRegistry.set(registry);
    return oldValue;
  }

  public WebServiceConnectionRegistry(final String name) {
    this(null, name, true);
  }

  public WebServiceConnectionRegistry(final String name, final Resource resource,
    final boolean readOnly) {
    super(null, name);
    setReadOnly(readOnly);
    setDirectory(resource);
    init();
  }

  protected WebServiceConnectionRegistry(final WebServiceConnectionManager connectionManager,
    final String name, final boolean visible) {
    super(connectionManager, name);
    setVisible(visible);
    init();
  }

  protected WebServiceConnectionRegistry(final WebServiceConnectionManager connectionManager,
    final String name, final Resource resource) {
    super(connectionManager, name);
    setDirectory(resource);
    init();
  }

  public WebServiceConnection addConnection(final Map<String, Object> config) {
    final WebServiceConnection connection = new WebServiceConnection(this, null, config);
    addConnection(connection);
    return connection;
  }

  public void addConnection(final String name, final WebService<?> webService) {
    final WebServiceConnection connection = new WebServiceConnection(this, name, webService);
    addConnection(connection);
  }

  public void addConnection(final WebServiceConnection connection) {
    String name = connection.getName();
    addConnection(name, connection);
  }

  @Override
  public String getIconName() {
    return "folder:world";
  }

  @Override
  protected WebServiceConnection loadConnection(final File webServiceFile) {
    final Map<String, ? extends Object> config = Json.toMap(webServiceFile);
    String name = Maps.getString(config, "name");
    if (!Property.hasValue(name)) {
      name = FileUtil.getBaseName(webServiceFile);
    }
    try {
      final WebServiceConnection webServiceConnection = new WebServiceConnection(this,
        webServiceFile.toString(), config);
      addConnection(name, webServiceConnection);
      return webServiceConnection;
    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass())
        .error("Error creating web service from: " + webServiceFile, e);
      return null;
    }
  }

  @Override
  public boolean removeConnection(final WebServiceConnection connection) {
    if (connection == null) {
      return false;
    } else {
      final String name = connection.getName();
      return removeConnection(name, connection);
    }
  }
}
