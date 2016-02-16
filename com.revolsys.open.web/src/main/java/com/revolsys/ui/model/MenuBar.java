package com.revolsys.ui.model;

public class MenuBar extends Menu {
  private String organizationUri;

  private String organizationName;

  private String organizationImageSrc;

  public String getOrganizationImageSrc() {
    return this.organizationImageSrc;
  }

  public String getOrganizationName() {
    return this.organizationName;
  }

  public String getOrganizationUri() {
    return this.organizationUri;
  }

  public void setOrganizationImageSrc(final String organizationImageSrc) {
    this.organizationImageSrc = organizationImageSrc;
  }

  public void setOrganizationName(final String organizationName) {
    this.organizationName = organizationName;
  }

  public void setOrganizationUri(final String organizationUri) {
    this.organizationUri = organizationUri;
  }
}
