package com.revolsys.io.page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MemoryPageManager implements PageManager {
  int pageSize = 64;

  private List<Page> pages = new ArrayList<Page>();

  private Set<Page> pagesInUse = new HashSet<Page>();

  private Set<Page> freePages = new TreeSet<Page>();

  public int getPageSize() {
    return pageSize;
  }

  public synchronized Page createPage() {
    Page page;
    if (freePages.isEmpty()) {
      page = new ByteArrayPage(this, pages.size(), pageSize);
      pages.add(page);
    } else {
      final Iterator<Page> iterator = freePages.iterator();
      page = iterator.next();
      iterator.remove();
    }
    pagesInUse.add(page);
    return page;
  }

  public synchronized Page getPage(int index) {
    Page page = pages.get(index);
    if (freePages.contains(page)) {
      throw new IllegalArgumentException("Page does not exist " + index);
    } else if (pagesInUse.contains(page)) {
      throw new IllegalArgumentException("Page is currently being used "
        + index);
    } else {
      pagesInUse.add(page);
      return page;
    }
  }

  public synchronized int getNumPages() {
    return pages.size() - freePages.size();
  }

  public Page createTempPage() {
    return new ByteArrayPage(this, -1, pageSize);
  }

  public void write(Page page) {
  }

  public synchronized void removePage(Page page) {
    freePages.add(page);
    pagesInUse.remove(page);
  }

  public synchronized void releasePage(Page page) {
    pagesInUse.remove(page);
  }
}
