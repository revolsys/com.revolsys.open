package com.revolsys.io.page;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.revolsys.collection.WeakCache;
import com.revolsys.io.FileUtil;

public class FilePageManager implements PageManager {
  int pageSize = 64;

  private RandomAccessFile randomAccessFile;

  // TODO 
  private final Map<Integer, Page> pages = new WeakCache<Integer, Page>();

  private final Set<Integer> freePageIndexes = new TreeSet<Integer>();

  private final Set<Page> pagesInUse = new HashSet<Page>();

  public FilePageManager() {
    this(FileUtil.createTempFile("pages", ".pf"));
  }

  public FilePageManager(final File file) {
    try {
      this.randomAccessFile = new RandomAccessFile(file, "rw");
    } catch (final FileNotFoundException e) {
      throw new IllegalArgumentException("Unable to open file "
        + file.getAbsolutePath(), e);
    }
  }

  @Override
  public synchronized Page createPage() {
    synchronized (pages) {
      Page page;
      if (freePageIndexes.isEmpty()) {
        try {
          final int index = (int)(randomAccessFile.length() / pageSize);
          page = new ByteArrayPage(this, index, pageSize);
          pages.put(page.getIndex(), page);
          write(page);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        final Iterator<Integer> iterator = freePageIndexes.iterator();
        final Integer pageIndex = iterator.next();
        iterator.remove();
        page = loadPage(pageIndex);
      }
      pagesInUse.add(page);
      return page;
    }
  }

  @Override
  public Page createTempPage() {
    return new ByteArrayPage(this, -1, pageSize);
  }

  @Override
  public int getNumPages() {
    return pages.size();
  }

  @Override
  public synchronized Page getPage(final int index) {
    synchronized (pages) {
      if (freePageIndexes.contains(index)) {
        throw new IllegalArgumentException("Page does not exist " + index);
      } else {
        Page page = pages.get(index);
        if (page == null) {
          page = loadPage(index);
        }
        if (pagesInUse.contains(page)) {
          throw new IllegalArgumentException("Page is currently being used "
            + index);
        } else {
          pagesInUse.add(page);
          page.setOffset(0);
          return page;
        }
      }
    }
  }

  @Override
  public int getPageSize() {
    return pageSize;
  }

  private Page loadPage(final int index) {
    try {
      final Page page = new ByteArrayPage(this, index, pageSize);
      randomAccessFile.seek(index * pageSize);
      final byte[] content = page.getContent();
      randomAccessFile.read(content);
      pages.put(index, page);
      return page;
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public synchronized void releasePage(final Page page) {
    write(page);
    pagesInUse.remove(page);
  }

  @Override
  public synchronized void removePage(final Page page) {
    synchronized (pages) {
      page.clear();
      write(page);
      freePageIndexes.add(page.getIndex());
    }
  }

  @Override
  public synchronized void write(final Page page) {
    if (page.getPageManager() == this) {
      synchronized (randomAccessFile) {
        try {
          final int index = page.getIndex();
          if (index >= 0) {
            randomAccessFile.seek(index * pageSize);
            final byte[] content = page.getContent();
            randomAccessFile.write(content);
          }
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

}
