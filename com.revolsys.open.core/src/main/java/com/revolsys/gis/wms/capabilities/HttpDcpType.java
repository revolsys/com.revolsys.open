package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

public class HttpDcpType extends DcpType {
  private List<HttpMethod> methods = new ArrayList<HttpMethod>();

  public HttpDcpType() {
    super("HTTP");
  }

  public void addMethod(final HttpMethod method) {
    this.methods.add(method);
  }

  public List<HttpMethod> getMethods() {
    return this.methods;
  }

  public void setMethods(final List<HttpMethod> methods) {
    this.methods = methods;
  }
}
