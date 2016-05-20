package com.revolsys.record.io.format.esri.rest;

import com.revolsys.webservice.WebServiceResource;

public interface CatalogElement extends WebServiceResource {

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
