package com.revolsys.webservice;

import java.util.List;
import java.util.Map;

import com.revolsys.collection.Parent;
import com.revolsys.io.connection.AbstractConnection;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.util.Exceptions;

public class WebServiceConnection extends
  AbstractConnection<WebServiceConnection, WebServiceConnectionRegistry> implements Parent<Object> {
  private WebService<?> webService;

  public WebServiceConnection(final WebServiceConnectionRegistry registry,
    final String resourceName, final Map<String, ? extends Object> config) {
    super(registry, resourceName, config);
  }

  public WebServiceConnection(final WebServiceConnectionRegistry registry, final String name,
    final WebService<?> webService) {
    super(registry, name);
    this.webService = webService;
  }

  @Override
  public List<Object> getChildren() {
    final WebService<Object> webService = getWebService();
    return webService.getChildren();
  }

  @Override
  public String getIconName() {
    return "world";
  }

  @Override
  public WebServiceConnectionRegistry getRegistry() {
    return super.getRegistry();
  }

  @SuppressWarnings("unchecked")
  public <W extends WebService<?>> W getWebService() {
    synchronized (this) {
      if (this.webService == null || this.webService.isClosed()) {
        this.webService = null;
        try {
          this.webService = MapObjectFactory.toObject(getProperties());
        } catch (final Throwable e) {
          Exceptions.throwUncheckedException(e);
        }
      }
    }
    return (W)this.webService;
  }

}
