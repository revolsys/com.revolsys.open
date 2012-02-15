package com.revolsys.io.page;

public interface PageManager {

  Page createPage();

  Page createTempPage();

  int getNumPages();

  Page getPage(int index);

  int getPageSize();

  void releasePage(Page page);

  void removePage(Page page);

  void write(Page page);
}
