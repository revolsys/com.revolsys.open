package com.revolsys.gis.wms.capabilities;

import java.net.URL;

public class FormatUrl {
  private String format;

  private URL onlineResource;

  public String getFormat() {
    return format;
  }

  public URL getOnlineResource() {
    return onlineResource;
  }

  public void setFormat(final String format) {
    this.format = format;
  }

  public void setOnlineResource(final URL onlineResource) {
    this.onlineResource = onlineResource;
  }

}
