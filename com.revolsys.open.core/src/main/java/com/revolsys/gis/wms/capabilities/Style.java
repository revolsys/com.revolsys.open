package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

public class Style {
  private String name;

  private String title;

  private String abstractDescription;

  private List<ImageUrl> legendUrls = new ArrayList<ImageUrl>();

  private FormatUrl styleSheetUrl;

  private FormatUrl styleUrl;

  public void addLegendUrl(final ImageUrl legendUrl) {
    this.legendUrls.add(legendUrl);

  }

  public String getAbstractDescription() {
    return this.abstractDescription;
  }

  public List<ImageUrl> getLegendUrls() {
    return this.legendUrls;
  }

  public String getName() {
    return this.name;
  }

  public FormatUrl getStyleSheetUrl() {
    return this.styleSheetUrl;
  }

  public FormatUrl getStyleUrl() {
    return this.styleUrl;
  }

  public String getTitle() {
    return this.title;
  }

  public void setAbstractDescription(final String abstractDescription) {
    this.abstractDescription = abstractDescription;
  }

  public void setLegendUrls(final List<ImageUrl> legendUrls) {
    this.legendUrls = legendUrls;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setStyleSheetUrl(final FormatUrl styleSheetUrl) {
    this.styleSheetUrl = styleSheetUrl;
  }

  public void setStyleUrl(final FormatUrl styleUrl) {
    this.styleUrl = styleUrl;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
