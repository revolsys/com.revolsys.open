package com.revolsys.io.page;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BPlusTree {
  private final PageManager pages = new FilePageManager();

  private final byte INTERIOR = 0;

  private final byte LEAF = 1;

  private final double fillFactor = 0.5;

  private final int minSize;

  private ByteArraySerializer keySerializer = ByteArraySerializer.INT;

  private ByteArraySerializer valueSerializer = ByteArraySerializer.INT;

  private ByteArraySerializer pageIndexSerializer = ByteArraySerializer.INT;

  public BPlusTree() {
    minSize = (int)(fillFactor * pages.getPageSize());
    if (pages.getNumPages() == 0) {
      final Page rootPage = pages.createPage();
      writePageHeader(rootPage, LEAF);
      printPage(rootPage);
    }
  }

  private void printPage(final Page page) {
    if (LOG.isDebugEnabled()) {
      final int offset = page.getOffset();
      page.setOffset(0);
      final byte pageType = page.readByte();
      final int numBytes = page.readShort();
      if (pageType == INTERIOR) {
        int pageIndex = readPageIndex(page);
        System.out.print("I");
        System.out.print(page.getIndex());
        System.out.print("\t");
        System.out.print(numBytes);
        System.out.print("\t");
        System.out.print(pageIndex);
        while (page.getOffset() < numBytes) {
          final Object value = valueSerializer.readFromPage(page);
          pageIndex = readPageIndex(page);
          System.out.print("<-");
          System.out.print(value);
          System.out.print("->");
          System.out.print(pageIndex);
        }
      } else if (pageType == LEAF) {
        System.out.print("L");
        System.out.print(page.getIndex());
        System.out.print("\t");
        System.out.print(numBytes);
        System.out.print("\t");
        boolean first = true;
        while (page.getOffset() < numBytes) {
          if (first) {
            first = false;
          } else {
            System.out.print(",");
          }
          final Object key = keySerializer.readFromPage(page);
          final Object value = valueSerializer.readFromPage(page);
          System.out.print(key);
          System.out.print("=");
          System.out.print(value);
        }
      }
      System.out.println();
      page.setOffset(offset);
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(BPlusTree.class);

  private int rootPageIndex = 0;

  public Object get(Object key) {
    return get(rootPageIndex, key);
  }

  public void put(final Object key, final Object value) {
    final List<Page> childPages = put(rootPageIndex, key, value);
    if (!childPages.isEmpty()) {
      final Page rootPage = childPages.get(0);
      final Page leftPage = pages.createPage();
      leftPage.setContent(rootPage);

      rootPage.clear();
      final byte pageType = INTERIOR;
      writePageHeader(rootPage, pageType);

      final int firstChildPageIndex = leftPage.getIndex();
      final byte[] firstChildPageIndexBytes = pageIndexSerializer.writeToByteArray(firstChildPageIndex);
      rootPage.writeBytes(firstChildPageIndexBytes);

      for (int i = 1; i < childPages.size(); i++) {
        final Page childPage = childPages.get(i);
        Object childFirstKey = getFirstKey(childPage);
        final byte[] keyBytes = keySerializer.writeToByteArray(childFirstKey);
        rootPage.writeBytes(keyBytes);

        final int childPageIndex = childPage.getIndex();
        final byte[] childPageIndexBytes = pageIndexSerializer.writeToByteArray(childPageIndex);
        rootPage.writeBytes(childPageIndexBytes);
      }
      setNumBytes(rootPage);
      rootPage.flush();
      printPage(rootPage);
    }
  }

  private Object getFirstKey(final Page page) {
    page.setOffset(0);
    int pageType = page.readByte();
    skipHeader(page);
    if (pageType == INTERIOR) {
      // skip pointer
      readPageIndex(page);
      Object firstKey = keySerializer.readFromPage(page);
      return firstKey;
    } else if (pageType == LEAF) {
      Object firstKey = keySerializer.readFromPage(page);
      return firstKey;
    } else {
      throw new IllegalArgumentException("Unknown page type");
    }
  }

  private void writePageHeader(final Page page, final byte pageType) {
    page.writeByte(pageType);
    page.writeShort((short)3);
  }

  protected List<Page> put(final int pageIndex, final Object key,
    final Object value) {
    final Page page = pages.getPage(pageIndex);
    page.setOffset(0);
    final byte pageType = page.readByte();
    if (pageType == INTERIOR) {
      return putInterior(page, key, value);
    } else if (pageType == LEAF) {
      return putLeaf(page, key, value);
    } else {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }
  }

  protected Object get(final int pageIndex, final Object key) {
    final Page page = pages.getPage(pageIndex);
    page.setOffset(0);
    final byte pageType = page.readByte();
    if (pageType == INTERIOR) {
      return getInterior(page, key);
    } else if (pageType == LEAF) {
      return getLeaf(page, key);
    } else {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }
  }

  private List<Page> putLeaf(final Page page, final Object key,
    final Object value) {
    List<Object> keys = new ArrayList<Object>();
    List<Object> values = new ArrayList<Object>();

    boolean newValueWritten = false;
    int numBytes = page.readShort();
    while (page.getOffset() < numBytes) {
      final Comparable<Object> currentKey = keySerializer.readFromPage(page);
      final Object currentValue = valueSerializer.readFromPage(page);
      int compare = currentKey.compareTo(key);
      if (compare > 0) {
        keys.add(key);
        values.add(value);
        newValueWritten = true;
      }
      keys.add(currentKey);
      values.add(currentValue);

    }
    if (!newValueWritten) {
      keys.add(key);
      values.add(value);
    }
    return updateOrSplitLeafPage(page, numBytes, keys, values);
  }

  private Object getLeaf(final Page page, final Object key) {
    int numBytes = page.readShort();
    while (page.getOffset() < numBytes) {
      final Comparable<Object> currentKey = keySerializer.readFromPage(page);
      final Object currentValue = valueSerializer.readFromPage(page);
      int compare = currentKey.compareTo(key);
      if (compare == 0) {
        return currentValue;
      }
    }
    return null;
  }

  public int readPageIndex(Page page) {
    int pageIndex = ((Number)pageIndexSerializer.readFromPage(page)).intValue();
    return pageIndex;
  }

  private List<Page> putInterior(final Page page, final Object key,
    final Object value) {
    List<Integer> pageIndexes = new ArrayList<Integer>();
    List<Object> keys = new ArrayList<Object>();
    int numBytes = page.readShort();
    int previousPageIndex = readPageIndex(page);
    pageIndexes.add(previousPageIndex);
    while (page.getOffset() < numBytes) {
      final Comparable<Object> currentKey = keySerializer.readFromPage(page);
      keys.add(currentKey);
      final int nextPageIndex = readPageIndex(page);
      pageIndexes.add(nextPageIndex);
      int compare = currentKey.compareTo(key);
      if (compare > 0) {
        final List<Page> childPages = put(previousPageIndex, key, value);
        if (childPages.isEmpty()) {
          return Collections.emptyList();
        } else {
          putChildInteriorPages(childPages, pageIndexes, keys);

          // Add remaining child keys and pages indexes
          while (page.getOffset() < numBytes) {
            final Object childKey = keySerializer.readFromPage(page);
            keys.add(childKey);
            final int childPageIndex = readPageIndex(page);
            pageIndexes.add(childPageIndex);
          }

          return updateOrSplitInteriorPage(page, numBytes, pageIndexes, keys);
        }

      }
      previousPageIndex = nextPageIndex;
    }
    final List<Page> childPages = put(previousPageIndex, key, value);
    if (childPages.isEmpty()) {
      return Collections.emptyList();
    } else {
      putChildInteriorPages(childPages, pageIndexes, keys);

      Page rightPage = childPages.get(1);
      skipHeader(rightPage);

      return updateOrSplitInteriorPage(page, numBytes, pageIndexes, keys);
    }
  }

  private Object getInterior(final Page page, final Object key) {
    int numBytes = page.readShort();
    int previousPageIndex = readPageIndex(page);
    while (page.getOffset() < numBytes) {
      final Comparable<Object> currentKey = keySerializer.readFromPage(page);
      final int nextPageIndex = readPageIndex(page);
      int compare = currentKey.compareTo(key);
      if (compare > 0) {
        return get(previousPageIndex, key);
      }
      previousPageIndex = nextPageIndex;
    }
    return get(previousPageIndex, key);
  }

  private void putChildInteriorPages(final List<Page> childPages,
    List<Integer> pageIndexes, List<Object> keys) {
    for (int i = 1; i < childPages.size(); i++) {
      final Page childPage = childPages.get(i);
      int childPageIndex = childPage.getIndex();
      Object childFirstKey = getFirstKey(childPage);
      keys.add(childFirstKey);
      pageIndexes.add(childPageIndex);
    }
  }

  private int headerSize = 3;

  private List<Page> updateOrSplitLeafPage(Page page, int oldNumBytes,
    List<Object> keys, List<Object> values) {
    List<byte[]> valuesBytes = new ArrayList<byte[]>();
    List<byte[]> keysBytes = new ArrayList<byte[]>();
    int numBytes = headerSize;
    int largeValueIndex = -1;
    int splitIndex = -1;
    int i = 0;
    while (i < keys.size()) {
      Object key = keys.get(i);
      byte[] keyBytes = keySerializer.writeToByteArray(key);
      keysBytes.add(keyBytes);
      numBytes += keyBytes.length;

      Object value = values.get(i);
      byte[] valueBytes = pageIndexSerializer.writeToByteArray(value);
      valuesBytes.add(valueBytes);
      numBytes += valueBytes.length;

      i++;
      if (splitIndex == -1 && numBytes > minSize) {
        splitIndex = i;
      }
    }

    if (numBytes < page.getSize()) {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, keysBytes.size());
      printPage(page);
      return Collections.emptyList();
    } else if (largeValueIndex >= 0) {
      throw new RuntimeException("Splitting not supported");
    } else {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, splitIndex);
      printPage(page);
      Page rightPage = pages.createPage();
      setLeafKeyAndValueBytes(rightPage, keysBytes, valuesBytes, splitIndex,
        keysBytes.size());
      printPage(rightPage);
      return Arrays.asList(page, rightPage);
    }
  }

  private List<Page> updateOrSplitInteriorPage(Page page, int oldNumBytes,
    List<Integer> pageIndexes, List<Object> keys) {
    List<byte[]> pageIndexesBytes = new ArrayList<byte[]>();
    List<byte[]> keysBytes = new ArrayList<byte[]>();
    int numBytes = 3;
    int largeValueIndex = -1;
    int splitIndex = -1;
    int i = 0;
    while (i < keys.size()) {
      int pageIndex = pageIndexes.get(i);
      byte[] pageIndexBytes = pageIndexSerializer.writeToByteArray(pageIndex);
      pageIndexesBytes.add(pageIndexBytes);
      numBytes += pageIndexBytes.length;

      if (i > 0 && splitIndex == -1 && numBytes > minSize) {
        splitIndex = i;
      }
      Object key = keys.get(i);
      byte[] keyBytes = keySerializer.writeToByteArray(key);
      keysBytes.add(keyBytes);
      numBytes += keyBytes.length;

      i++;
    }
    int pageIndex = pageIndexes.get(i);
    byte[] pageIndexBytes = pageIndexSerializer.writeToByteArray(pageIndex);
    pageIndexesBytes.add(pageIndexBytes);
    numBytes += pageIndexBytes.length;

    if (numBytes < page.getSize()) {
      setInteriorIndexAndKeyBytes(page, pageIndexesBytes, keysBytes, 0,
        keysBytes.size());
      printPage(page);
      return Collections.emptyList();
    } else if (largeValueIndex >= 0) {
      throw new RuntimeException("Splitting not supported");
    } else {
      setInteriorIndexAndKeyBytes(page, pageIndexesBytes, keysBytes, 0,
        splitIndex);
      Page rightPage = pages.createPage();
      setInteriorIndexAndKeyBytes(rightPage, pageIndexesBytes, keysBytes,
        splitIndex, keysBytes.size());
      printPage(page);
      printPage(rightPage);
      return Arrays.asList(page, rightPage);
    }
  }

  private void setInteriorIndexAndKeyBytes(Page page,
    List<byte[]> pageIndexesBytes, List<byte[]> keysBytes, int startIndex,
    int endIndex) {
    page.setOffset(0);
    page.writeByte(INTERIOR);
    page.writeShort((short)0);
    int i = startIndex;
    for (; i < endIndex; i++) {
      byte[] pageIndexBytes = pageIndexesBytes.get(i);
      page.writeBytes(pageIndexBytes);
      byte[] keyBytes = keysBytes.get(i);
      page.writeBytes(keyBytes);
    }
    byte[] pageIndexBytes = pageIndexesBytes.get(i);
    page.writeBytes(pageIndexBytes);
    setNumBytes(page);
    page.clearBytes(page.getOffset());
    page.flush();
  }

  private void setLeafKeyAndValueBytes(Page page, List<byte[]> keysBytes,
    List<byte[]> valuesBytes, int startIndex, int endIndex) {
    page.setOffset(0);
    page.writeByte(LEAF);
    page.writeShort((short)0);
    int i = startIndex;
    for (; i < endIndex; i++) {
      byte[] keyBytes = keysBytes.get(i);
      page.writeBytes(keyBytes);
      byte[] valueBytes = valuesBytes.get(i);
      page.writeBytes(valueBytes);
    }
    setNumBytes(page);
    page.clearBytes(page.getOffset());
    page.flush();
  }

  private void skipHeader(Page page) {
    page.setOffset(3);
  }

  private void setNumBytes(final Page page) {
    int offset = page.getOffset();
    page.setOffset(1);
    page.writeShort((short)offset);
    page.setOffset(offset);
  }
}
