package com.revolsys.webservice;

import com.revolsys.collection.NameProxy;
import com.revolsys.io.PathName;
import com.revolsys.util.IconNameProxy;

public interface WebServiceResource extends NameProxy, IconNameProxy {
  default <R extends WebServiceResource> R getChild(final String name) {
    return null;
  }

  default <R extends WebServiceResource> R getParent() {
    return null;
  }

  default String getPathElement() {
    return getName();
  }

  default PathName getPathName() {
    final WebServiceResource parent = getParent();
    if (parent == null) {
      return PathName.ROOT;
    } else {
      final PathName parentPathName = parent.getPathName();
      final String name = getPathElement();
      return parentPathName.newChild(name);
    }
  }

  default boolean isHasError() {
    return false;
  }
}
