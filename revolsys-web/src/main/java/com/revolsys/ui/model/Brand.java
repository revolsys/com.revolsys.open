package com.revolsys.ui.model;

public class Brand {
  private String brandImageSrc;

  private String brandSmallImageSrc;

  private String brandUri;

  private String brandTitle;

  public String getBrandImageSrc() {
    return this.brandImageSrc;
  }

  public String getBrandSmallImageSrc() {
    return this.brandSmallImageSrc;
  }

  public String getBrandTitle() {
    return this.brandTitle;
  }

  public String getBrandUri() {
    return this.brandUri;
  }

  public void setBrandImageSrc(final String brandImageSrc) {
    this.brandImageSrc = brandImageSrc;
  }

  public void setBrandSmallImageSrc(final String brandSmallImageSrc) {
    this.brandSmallImageSrc = brandSmallImageSrc;
  }

  public void setBrandTitle(final String brandTitle) {
    this.brandTitle = brandTitle;
  }

  public void setBrandUri(final String brandUri) {
    this.brandUri = brandUri;
  }
}
