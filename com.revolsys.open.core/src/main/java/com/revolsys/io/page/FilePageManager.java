package com.revolsys.io.page;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import com.revolsys.io.FileUtil;

public class FilePageManager implements PageManager {
  int pageSize = 64;

  private RandomAccessFile randomAccessFile;

  private WeakHashMap<Integer, Page> pages = new WeakHashMap<Integer, Page>();

  private Set<Integer> freePageIndexes = new TreeSet<Integer>();

  public FilePageManager() {
    this(FileUtil.createTempFile("pages", ".pf"));
  }

  public FilePageManager(File file) {
    try {
      this.randomAccessFile = new RandomAccessFile(file, "rw");
    } catch (FileNotFoundException e) {
      throw new IllegalArgumentException("Unable to open file "
        + file.getAbsolutePath(), e);
    }
  }

  public synchronized void removePage(Page page) {
    synchronized (pages) {
      page.clear();
      write(page);
      freePageIndexes.add(page.getIndex());
    }
  }

  public int getPageSize() {
    return pageSize;
  }

  public synchronized Page createPage() {
    synchronized (pages) {
      Page page;
      if (freePageIndexes.isEmpty()) {
        try {
          int index = (int)(randomAccessFile.length() / pageSize);
          page = new ByteArrayPage(this, index, pageSize);
          pages.put(page.getIndex(), page);
          write(page);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      } else {
        final Iterator<Integer> iterator = freePageIndexes.iterator();
        Integer pageIndex = iterator.next();
        iterator.remove();
        page = loadPage(pageIndex);
      }
      pagesInUse.add(page);
      return page;
    }
  }

  public synchronized Page getPage(int index) {
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
          return page;
        }
      }
    }
  }

  private Page loadPage(int index) {
    try {
      Page page = new ByteArrayPage(this, index, pageSize);
      randomAccessFile.seek(index * pageSize);
      final byte[] content = page.getContent();
      randomAccessFile.read(content);
      pages.put(index, page);
      return page;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public int getNumPages() {
    return pages.size();
  }

  public Page createTempPage() {
    return new ByteArrayPage(this, -1, pageSize);
  }

  public synchronized void write(Page page) {
    if (page.getPageManager() == this) {
      synchronized (randomAccessFile) {
        try {
          final int index = page.getIndex();
          if (index >= 0) {
            randomAccessFile.seek(index * pageSize);
            final byte[] content = page.getContent();
            randomAccessFile.write(content);
          }
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public synchronized void releasePage(Page page) {
    write(page);
    pagesInUse.remove(page);
  }

  private Set<Page> pagesInUse = new HashSet<Page>();

}
