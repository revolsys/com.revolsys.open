package com.revolsys.record.io.format.esri.rest;

import com.revolsys.collection.NameProxy;
import com.revolsys.io.PathName;
import com.revolsys.util.IconNameProxy;

public interface CatalogElement extends IconNameProxy, NameProxy {
  default <C extends CatalogElement> C getChild(final String name) {
    return null;
  }

  CatalogElement getParent();

  default String getPathElement() {
    return getName();
  }

  default PathName getPathName() {
    final CatalogElement parent = getParent();
    if (parent == null) {
      return PathName.ROOT;
    } else {
      final PathName parentPathName = parent.getPathName();
      final String name = getPathElement();
      return parentPathName.newChild(name);
    }
  }

  String getResourceUrl();

  default String getResourceUrl(final String child) {
    final String resourceUrl = getResourceUrl();
    if (isUseProxy()) {
      return resourceUrl + "%2F" + child;
    } else {
      return resourceUrl + '/' + child;
    }
  }

  default CatalogElement getRoot() {
    CatalogElement element = this;
    for (CatalogElement parent = element.getParent(); parent != null; parent = element
      .getParent()) {
      element = parent;
    }
    return element;
  }

  default String getRootServiceUrl() {
    return getRoot().getResourceUrl();
  }

  default boolean isUseProxy() {
    return false;
  }
}
