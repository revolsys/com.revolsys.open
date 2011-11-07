package com.revolsys.io.page;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.WeakHashMap;

import com.revolsys.io.FileUtil;

public class FilePageManager implements PageManager {
  int pageSize = 64;

  private RandomAccessFile randomAccessFile;

  private WeakHashMap<Integer, Page> pages = new WeakHashMap<Integer, Page>();

  private Set<Page> freePages = new TreeSet<Page>();

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

  public void freePage(Page page) {
    freePages.add(page);
  }

  public int getPageSize() {
    return pageSize;
  }

  public Page createPage() {
    if (freePages.isEmpty()) {
      try {
        int index = (int)(randomAccessFile.length() / pageSize);
        Page page = new ByteArrayPage(this, index, pageSize);
        pages.put(page.getIndex(), page);
        write(page);
        return page;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      final Iterator<Page> iterator = freePages.iterator();
      Page page = iterator.next();
      iterator.remove();
      return page;
    }
  }

  public Page getPage(int index) {
    synchronized (pages) {
      try {
        Page page = pages.get(index);
        if (page == null) {
          page = new ByteArrayPage(this, index, pageSize);
          randomAccessFile.seek(index * pageSize);
          final byte[] content = page.getContent();
          randomAccessFile.read(content);
          pages.put(index, page);
        }
        return page;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  public int getNumPages() {
    return pages.size();
  }

  public Page createTempPage() {
    return new ByteArrayPage(this, -1, pageSize);
  }

  public void write(Page page) {
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
}
