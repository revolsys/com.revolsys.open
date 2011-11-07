package com.revolsys.io.page;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MemoryPageManager implements PageManager {
  int pageSize = 64;

  private List<Page> pages = new ArrayList<Page>();

  private Set<Page> freePages = new TreeSet<Page>();

  public void freePage(Page page) {
    freePages.add(page);
  }

  public int getPageSize() {
    return pageSize;
  }

  public Page createPage() {
    if (freePages.isEmpty()) {
      Page page = new ByteArrayPage(this, pages.size(), pageSize);
      pages.add(page);
      return page;
    } else {
      final Iterator<Page> iterator = freePages.iterator();
      Page page = iterator.next();
      iterator.remove();
      return page;
    }
  }

  public Page getPage(int index) {
    return pages.get(index);
  }

  public int getNumPages() {
    return pages.size();
  }

  public Page createTempPage() {
    return new ByteArrayPage(this, -1, pageSize);
  }

  public void write(Page page) {
  }
}
