package com.revolsys.gis.wms.capabilities;

import java.net.URL;

public class Attribution {
  private String title;

  private URL onlineResource;

  private ImageUrl logoUrl;

  public ImageUrl getLogoUrl() {
    return this.logoUrl;
  }

  public URL getOnlineResource() {
    return this.onlineResource;
  }

  public String getTitle() {
    return this.title;
  }

  public void setLogoUrl(final ImageUrl logoUrl) {
    this.logoUrl = logoUrl;
  }

  public void setOnlineResource(final URL onlineResource) {
    this.onlineResource = onlineResource;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
