package com.revolsys.ui.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class PageInfo {
  private String title;

  private String description;

  private Map<String, PageInfo> pages = new LinkedHashMap<String, PageInfo>();

  public PageInfo() {
  }

  public PageInfo(final String title, final String description) {
    this.title = title;
    this.description = description;
  }

  public void addPage(final String path, final PageInfo page) {
    pages.put(path, page);
  }

  public String getDescription() {
    return description;
  }

  public Map<String, PageInfo> getPages() {
    return pages;
  }

  public String getTitle() {
    return title;
  }

  public void setDescription(final String description) {
    this.description = description;
  }

  public void setPages(final Map<String, PageInfo> pages) {
    this.pages = pages;
  }

  public void setTitle(final String title) {
    this.title = title;
  }

}
