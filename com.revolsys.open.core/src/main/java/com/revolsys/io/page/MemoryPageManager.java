package com.revolsys.io.page;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class MemoryPageManager implements PageManager {
  int pageSize = 64;

  private final List<Page> pages = new ArrayList<Page>();

  private final Set<Page> pagesInUse = new HashSet<Page>();

  private final Set<Page> freePages = new TreeSet<Page>();

  @Override
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

  @Override
  public Page createTempPage() {
    return new ByteArrayPage(this, -1, pageSize);
  }

  @Override
  public synchronized int getNumPages() {
    return pages.size() - freePages.size();
  }

  @Override
  public synchronized Page getPage(final int index) {
    final Page page = pages.get(index);
    if (freePages.contains(page)) {
      throw new IllegalArgumentException("Page does not exist " + index);
    } else if (pagesInUse.contains(page)) {
      throw new IllegalArgumentException("Page is currently being used "
        + index);
    } else {
      page.setOffset(0);
      pagesInUse.add(page);
      return page;
    }
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  @Override
  public synchronized void releasePage(final Page page) {
    pagesInUse.remove(page);
  }

  @Override
  public synchronized void removePage(final Page page) {
    freePages.add(page);
    pagesInUse.remove(page);
  }

  @Override
  public void write(final Page page) {
  }
}
