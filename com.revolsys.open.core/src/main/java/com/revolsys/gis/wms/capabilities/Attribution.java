package com.revolsys.gis.wms.capabilities;

import java.net.URL;

public class Attribution {
  private ImageUrl logoUrl;

  private URL onlineResource;

  private String title;

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
