package com.revolsys.webservice;

import com.revolsys.collection.NameProxy;
import com.revolsys.util.IconNameProxy;

public interface WebServiceResource extends NameProxy, IconNameProxy {
  default <R extends WebServiceResource> R getChild(final String name) {
    return null;
  }
}
