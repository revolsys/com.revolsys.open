package com.revolsys.gis.wms.capabilities;

import java.net.URL;

public class HttpMethod {
  private String name;

  private URL onlineResource;

  public String getName() {
    return name;
  }

  public URL getOnlineResource() {
    return onlineResource;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setOnlineResource(final URL onlineResource) {
    this.onlineResource = onlineResource;
  }

  @Override
  public String toString() {
    return name + " " + onlineResource;
  }
}
