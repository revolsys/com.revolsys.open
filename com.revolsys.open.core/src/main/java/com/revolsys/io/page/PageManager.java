package com.revolsys.io.page;

public interface PageManager {

  Page getPage(int index);

  int getNumPages();

  Page createPage();

  int getPageSize();

  Page createTempPage();

  void write(Page page);

  void removePage(Page dataPage);
}
