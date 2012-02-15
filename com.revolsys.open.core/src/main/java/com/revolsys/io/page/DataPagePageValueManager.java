package com.revolsys.io.page;

import java.util.ArrayList;
import java.util.List;

public class DataPagePageValueManager<T> implements PageValueManager<T> {

  public static <T> PageValueManager<T> create(
    final PageManager pageManager,
    final PageValueManager<T> valueSerializer) {
    return new DataPagePageValueManager<T>(pageManager, valueSerializer);
  }

  private final PageManager pageManager;

  private final PageValueManager<T> valueSerializer;

  public DataPagePageValueManager(final PageManager pageManager,
    final PageValueManager<T> valueSerializer) {
    this.pageManager = pageManager;
    this.valueSerializer = valueSerializer;
  }

  public void disposeBytes(final byte[] bytes) {
    final int pageIndex = MethodPageValueManager.getIntValue(bytes);
    Page dataPage = pageManager.getPage(pageIndex);
    dataPage.setOffset(0);
    byte pageType = dataPage.readByte();
    while (pageType == BPlusTree.EXTENDED) {
      BPlusTree.skipHeader(dataPage);
      final int nextPageIndex = dataPage.readInt();
      pageManager.removePage(dataPage);
      dataPage = pageManager.getPage(nextPageIndex);
      dataPage.setOffset(0);
      pageType = dataPage.readByte();
    }
    if (pageType == BPlusTree.DATA) {
      pageManager.removePage(dataPage);
      pageManager.releasePage(dataPage);
    } else {
      throw new IllegalArgumentException("Expecting a data page "
        + BPlusTree.DATA + " not " + pageType);
    }
  }

  public byte[] getBytes(final Page page) {
    return page.readBytes(4);
  }

  public byte[] getBytes(final T value) {
    final byte[] valueBytes = valueSerializer.getBytes(value);

    int offset = 0;
    final int pageSize = pageManager.getPageSize();
    Page page = pageManager.createPage();
    try {
      final int pageIndex = page.getIndex();
      while (valueBytes.length + 3 > offset + pageSize) {
        final Page nextPage = pageManager.createPage();
        BPlusTree.writePageHeader(page, BPlusTree.EXTENDED);
        page.writeInt(nextPage.getIndex());
        page.writeBytes(valueBytes, offset, pageSize - 7);
        BPlusTree.setNumBytes(page);
        pageManager.releasePage(page);
        page = nextPage;
        offset += pageSize - 7;
      }

      BPlusTree.writePageHeader(page, BPlusTree.DATA);
      page.writeBytes(valueBytes, offset, valueBytes.length - offset);
      BPlusTree.setNumBytes(page);

      return MethodPageValueManager.INT.getBytes(pageIndex);
    } finally {
      pageManager.releasePage(page);
    }
  }

  public <V extends T> V getValue(final byte[] indexBytes) {
    final int pageIndex = MethodPageValueManager.getIntValue(indexBytes);
    Page dataPage = pageManager.getPage(pageIndex);
    try {
      dataPage.setOffset(0);
      byte pageType = dataPage.readByte();
      final List<byte[]> pageBytes = new ArrayList<byte[]>();
      int size = 0;
      while (pageType == BPlusTree.EXTENDED) {
        final int numBytes = dataPage.readShort() - 7;
        final int nextPageIndex = dataPage.readInt();
        final byte[] bytes = dataPage.readBytes(numBytes);
        pageBytes.add(bytes);
        size += bytes.length;
        pageManager.releasePage(dataPage);
        dataPage = pageManager.getPage(nextPageIndex);
        dataPage.setOffset(0);
        pageType = dataPage.readByte();
      }
      if (pageType == BPlusTree.DATA) {
        final int numBytes = dataPage.readShort() - 3;
        final byte[] bytes = dataPage.readBytes(numBytes);
        pageBytes.add(bytes);
        size += bytes.length;

      } else {
        throw new IllegalArgumentException("Expecting a data page "
          + BPlusTree.DATA + " not " + pageType);
      }
      final byte[] valueBytes = new byte[size];
      int offset = 0;
      for (final byte[] bytes : pageBytes) {
        System.arraycopy(bytes, 0, valueBytes, offset, bytes.length);
        offset += bytes.length;
      }
      return valueSerializer.getValue(valueBytes);
    } finally {
      pageManager.releasePage(dataPage);
    }
  }

  //
  // public <V extends T> V removeFromPage(Page page) {
  // final int pageIndex = page.readInt();
  // Page dataPage = pageManager.getPage(pageIndex);
  // dataPage.setOffset(0);
  // byte pageType = dataPage.readByte();
  // List<byte[]> pageBytes = new ArrayList<byte[]>();
  // int size = 0;
  // while (pageType == BPlusTree.EXTENDED) {
  // int numBytes = dataPage.readShort() - 7;
  // int nextPageIndex = dataPage.readInt();
  // byte[] bytes = dataPage.readBytes(numBytes);
  // pageBytes.add(bytes);
  // size += bytes.length;
  // pageManager.removePage(dataPage);
  // dataPage = pageManager.getPage(nextPageIndex);
  // dataPage.setOffset(0);
  // pageType = dataPage.readByte();
  // }
  // if (pageType == BPlusTree.DATA) {
  // final int numBytes = dataPage.readShort() - 3;
  // final byte[] bytes = dataPage.readBytes(numBytes);
  // pageBytes.add(bytes);
  // size += bytes.length;
  // pageManager.removePage(dataPage);
  // } else {
  // throw new IllegalArgumentException("Expecting a data page "
  // + BPlusTree.DATA + " not " + pageType);
  // }
  // byte[] valueBytes = new byte[size];
  // int offset = 0;
  // for (byte[] bytes : pageBytes) {
  // System.arraycopy(bytes, 0, valueBytes, offset, bytes.length);
  // offset += bytes.length;
  // }
  // return valueSerializer.readFromByteArray(valueBytes);
  // }

  public <V extends T> V readFromPage(final Page page) {
    final byte[] indexBytes = getBytes(page);
    return getValue(indexBytes);
  }

}
