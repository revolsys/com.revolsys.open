package com.revolsys.record.io.format.esri.rest;

import com.revolsys.collection.NameProxy;
import com.revolsys.util.IconNameProxy;

public interface CatalogElement extends IconNameProxy, NameProxy {
  default <C extends CatalogElement> C getChild(final String name) {
    return null;
  }

  CatalogElement getParent();

  default CatalogElement getRoot() {
    CatalogElement element = this;
    for (CatalogElement parent = element.getParent(); parent != null; parent = element
      .getParent()) {
      element = parent;
    }
    return element;
  }

  default String getRootServiceUrl() {
    return getRoot().getServiceUrl();
  }

  String getServiceUrl();
}
