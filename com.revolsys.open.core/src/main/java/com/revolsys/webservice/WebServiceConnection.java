package com.revolsys.webservice;

import java.util.List;
import java.util.Map;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.collection.map.Maps;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.util.Exceptions;
import com.revolsys.util.JavaBeanUtil;
import com.revolsys.util.Property;

public class WebServiceConnection implements MapSerializer, Parent<Object> {
  private MapEx config;

  private String name;

  private WebService<?> webService;

  private WebServiceConnectionRegistry registry;

  public WebServiceConnection(final WebServiceConnectionRegistry registry,
    final String resourceName, final Map<String, ? extends Object> config) {
    this.registry = registry;
    setConfig(config);
    if (!Property.hasValue(this.name)) {
      this.name = FileUtil.getBaseName(resourceName);
    }
  }

  public WebServiceConnection(final WebServiceConnectionRegistry registry, final String name,
    final WebService<?> webService) {
    this.registry = registry;
    this.name = name;
    this.webService = webService;
  }

  public void delete() {
    if (this.registry != null) {
      this.registry.removeConnection(this);
    }
    this.config = null;
    this.webService = null;
    this.name = null;
    this.registry = null;

  }

  @Override
  public List<Object> getChildren() {
    final WebService<Object> webService = getWebService();
    return webService.getChildren();
  }

  public Map<String, Object> getConfig() {
    return JavaBeanUtil.clone(this.config);
  }

  @Override
  public String getIconName() {
    return "world";
  }

  public String getName() {
    return this.name;
  }

  public WebServiceConnectionRegistry getRegistry() {
    return this.registry;
  }

  @SuppressWarnings("unchecked")
  public <W extends WebService<?>> W getWebService() {
    synchronized (this) {
      if (this.webService == null || this.webService.isClosed()) {
        this.webService = null;
        try {
          this.webService = MapObjectFactory.toObject(this.config);
        } catch (final Throwable e) {
          Exceptions.throwUncheckedException(e);
        }
      }
    }
    return (W)this.webService;
  }

  public boolean isReadOnly() {
    if (this.registry == null) {
      return true;
    } else {
      return this.registry.isReadOnly();
    }
  }

  public void setConfig(final Map<String, ? extends Object> config) {
    this.config = Maps.newLinkedHashEx(config);
    this.name = Maps.getString(this.config, "name", this.name);
  }

  @Override
  public MapEx toMap() {
    return this.config;
  }

  @Override
  public String toString() {
    return this.name;
  }
}
