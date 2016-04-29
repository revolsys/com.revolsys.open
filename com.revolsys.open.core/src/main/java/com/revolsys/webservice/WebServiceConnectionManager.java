package com.revolsys.webservice;

import java.io.File;

import com.revolsys.io.connection.AbstractConnectionRegistryManager;
import com.revolsys.spring.resource.FileSystemResource;
import com.revolsys.spring.resource.Resource;
import com.revolsys.util.OS;

public class WebServiceConnectionManager
  extends AbstractConnectionRegistryManager<WebServiceConnectionRegistry, WebServiceConnection> {
  public static final String WEB_SERVICES = "Web Services";

  private static final WebServiceConnectionManager INSTANCE;

  static {
    INSTANCE = new WebServiceConnectionManager();
    final File webServicesDirectory = OS
      .getApplicationDataDirectory("com.revolsys.gis/Web Services");
    INSTANCE.addConnectionRegistry("User", new FileSystemResource(webServicesDirectory));
  }

  public static WebServiceConnectionManager get() {
    return INSTANCE;
  }

  public WebServiceConnectionManager() {
    super(WEB_SERVICES);
  }

  public WebServiceConnectionRegistry addConnectionRegistry(final String name,
    final boolean visible) {
    final WebServiceConnectionRegistry registry = new WebServiceConnectionRegistry(this, name,
      visible);
    addConnectionRegistry(registry);
    return registry;
  }

  public WebServiceConnectionRegistry addConnectionRegistry(final String name,
    final Resource recordStoresDirectory) {
    final WebServiceConnectionRegistry registry = new WebServiceConnectionRegistry(this, name,
      recordStoresDirectory);
    addConnectionRegistry(registry);
    return registry;
  }

}
