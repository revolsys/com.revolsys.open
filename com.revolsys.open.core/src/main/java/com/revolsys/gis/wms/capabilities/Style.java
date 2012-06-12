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
    legendUrls.add(legendUrl);

  }

  public String getAbstractDescription() {
    return abstractDescription;
  }

  public List<ImageUrl> getLegendUrls() {
    return legendUrls;
  }

  public String getName() {
    return name;
  }

  public FormatUrl getStyleSheetUrl() {
    return styleSheetUrl;
  }

  public FormatUrl getStyleUrl() {
    return styleUrl;
  }

  public String getTitle() {
    return title;
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
