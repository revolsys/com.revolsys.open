package com.revolsys.webservice;

import java.util.List;

import com.revolsys.collection.Parent;
import com.revolsys.io.PathName;
import com.revolsys.io.map.MapSerializer;

public interface WebService<V> extends MapSerializer, Parent<V>, WebServiceResource {
  @Override
  String getName();

  @SuppressWarnings("unchecked")
  default <T extends WebServiceResource> T getWebServiceResource(final PathName pathName) {
    final List<String> elements = pathName.getElements();
    if (elements.isEmpty()) {
      return (T)this;
    } else {
      WebServiceResource resource = getChild(elements.get(0));
      for (int i = 1; resource != null && i < elements.size(); i++) {
        final String childLayerName = elements.get(i);
        resource = resource.getChild(childLayerName);
      }
      return (T)resource;
    }
  }

  @SuppressWarnings("unchecked")
  default <T extends WebServiceResource> T getWebServiceResource(final PathName pathName,
    final Class<T> elementClass) {
    if (pathName != null) {
      final List<String> elements = pathName.getElements();
      if (!elements.isEmpty()) {
        final String firstElementName = elements.get(0);
        WebServiceResource resource = getChild(firstElementName);
        for (int i = 1; resource != null && i < elements.size(); i++) {
          final String childLayerName = elements.get(i);
          resource = resource.getChild(childLayerName);
        }
        if (resource == null) {
          return null;
        } else if (elementClass.isAssignableFrom(resource.getClass())) {
          return (T)resource;
        } else {
          return null;
        }
      }
    }
    return null;
  }

  default boolean isClosed() {
    return false;
  }

  void setName(String name);
}
