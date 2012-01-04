package com.revolsys.io.page;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BPlusTree<K, V> extends AbstractMap<K, V> {
  public static final byte INTERIOR = 0;

  public static final byte LEAF = 1;

  public static final byte DATA = 2;

  public static final byte EXTENDED = -128;

  public static <K, V> BPlusTree<K, V> create(
    final PageManager pages,
    final PageValueManager<K> keyManager,
    final PageValueManager<V> valueManager) {
    return new BPlusTree<K, V>(pages, keyManager, valueManager);
  }

  public static <K, V> BPlusTree<K, V> createInMemory(
    final PageValueManager<K> keyManager,
    final PageValueManager<V> valueManager) {
    final MemoryPageManager pages = new MemoryPageManager();
    return new BPlusTree<K, V>(pages, keyManager, valueManager);
  }

  protected static void setNumBytes(final Page page) {
    final int offset = page.getOffset();
    page.setOffset(1);
    page.writeShort((short)offset);
    page.setOffset(offset);
  }

  protected static void skipHeader(final Page page) {
    page.setOffset(3);
  }

  protected static void writePageHeader(final Page page, final byte pageType) {
    page.writeByte(pageType);
    page.writeShort((short)3);
  }

  private final double fillFactor = 0.5;

  private final int headerSize = 3;

  private final PageValueManager<K> keyManager;

  private final int minSize;

  private final PageValueManager<Integer> pageIndexManager = MethodPageValueManager.INT;

  private final PageManager pages;

  private final int rootPageIndex = 0;

  private final PageValueManager<V> valueManager;

  public BPlusTree(final PageManager pages,
    final PageValueManager<K> keyManager, final PageValueManager<V> valueManager) {
    this.pages = pages;
    this.keyManager = keyManager;
    this.valueManager = valueManager;
    minSize = (int)(fillFactor * pages.getPageSize());
    if (pages.getNumPages() == 0) {
      final Page rootPage = pages.createPage();
      writePageHeader(rootPage, LEAF);
    }
  }

  protected V get(final int pageIndex, final K key) {
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

  public V get(final Object key) {
    return get(rootPageIndex, (K)key);
  }

  private K getFirstKey(final Page page) {
    page.setOffset(0);
    final int pageType = page.readByte();
    skipHeader(page);
    if (pageType == INTERIOR) {
      // skip pointer
      readPageIndex(page);
      final K firstKey = keyManager.readFromPage(page);
      return firstKey;
    } else if (pageType == LEAF) {
      final K firstKey = keyManager.readFromPage(page);
      return firstKey;
    } else {
      throw new IllegalArgumentException("Unknown page type");
    }
  }

  @SuppressWarnings("unchecked")
  private V getInterior(final Page page, final K key) {
    final int numBytes = page.readShort();
    int previousPageIndex = readPageIndex(page);
    while (page.getOffset() < numBytes) {
      final K currentKey = keyManager.readFromPage(page);
      final int nextPageIndex = readPageIndex(page);
      final int compare = ((Comparable<K>)currentKey).compareTo(key);
      if (compare > 0) {
        return get(previousPageIndex, key);
      }
      previousPageIndex = nextPageIndex;
    }
    return get(previousPageIndex, key);
  }

  @SuppressWarnings("unchecked")
  private V getLeaf(final Page page, final K key) {
    final int numBytes = page.readShort();
    while (page.getOffset() < numBytes) {
      final K currentKey = keyManager.readFromPage(page);
      final V currentValue = valueManager.readFromPage(page);
      final int compare = ((Comparable<K>)currentKey).compareTo(key);
      if (compare == 0) {
        return currentValue;
      }
    }
    return null;
  }

  public void print() {
    printPage(rootPageIndex);
  }

  private void printPage(final int pageIndex) {
    final Page page = pages.getPage(pageIndex);

    final List<Integer> pageIndexes = new ArrayList<Integer>();
    final int offset = page.getOffset();
    page.setOffset(0);
    final byte pageType = page.readByte();
    final int numBytes = page.readShort();
    if (pageType == INTERIOR) {
      int childPageIndex = readPageIndex(page);
      pageIndexes.add(childPageIndex);
      System.out.print("I");
      System.out.print(page.getIndex());
      System.out.print("\t");
      System.out.print(numBytes);
      System.out.print("\t");
      System.out.print(pageIndex);
      while (page.getOffset() < numBytes) {
        final K value = keyManager.readFromPage(page);
        childPageIndex = readPageIndex(page);
        pageIndexes.add(childPageIndex);
        System.out.print("<-");
        System.out.print(value);
        System.out.print("->");
        System.out.print(childPageIndex);
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
        final K key = keyManager.readFromPage(page);
        final V value = valueManager.readFromPage(page);
        System.out.print(key);
        System.out.print("=");
        System.out.print(value);
      }
    }
    System.out.println();
    page.setOffset(offset);
    for (final Integer childPageIndex : pageIndexes) {
      printPage(childPageIndex);
    }
  }

  protected List<Page> put(final int pageIndex, final K key, final V value) {
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

  public V put(final K key, final V value) {
    final List<Page> childPages = put(rootPageIndex, key, value);
    if (!childPages.isEmpty()) {
      final Page rootPage = childPages.get(0);
      final Page leftPage = pages.createPage();
      leftPage.setContent(rootPage);

      rootPage.clear();
      final byte pageType = INTERIOR;
      writePageHeader(rootPage, pageType);

      final int firstChildPageIndex = leftPage.getIndex();
      final byte[] firstChildPageIndexBytes = pageIndexManager.writeToByteArray(firstChildPageIndex);
      rootPage.writeBytes(firstChildPageIndexBytes);

      for (int i = 1; i < childPages.size(); i++) {
        final Page childPage = childPages.get(i);
        final K childFirstKey = getFirstKey(childPage);
        final byte[] keyBytes = keyManager.writeToByteArray(childFirstKey);
        rootPage.writeBytes(keyBytes);

        final int childPageIndex = childPage.getIndex();
        final byte[] childPageIndexBytes = pageIndexManager.writeToByteArray(childPageIndex);
        rootPage.writeBytes(childPageIndexBytes);
      }
      setNumBytes(rootPage);
      rootPage.flush();
    }
    return null;
  }

  private void putChildInteriorPages(
    final List<Page> childPages,
    final List<Integer> pageIndexes,
    final List<K> keys) {
    for (int i = 1; i < childPages.size(); i++) {
      final Page childPage = childPages.get(i);
      final int childPageIndex = childPage.getIndex();
      final K childFirstKey = getFirstKey(childPage);
      keys.add(childFirstKey);
      pageIndexes.add(childPageIndex);
    }
  }

  private List<Page> putInterior(final Page page, final K key, final V value) {
    final List<Integer> pageIndexes = new ArrayList<Integer>();
    final List<K> keys = new ArrayList<K>();
    final int numBytes = page.readShort();
    int previousPageIndex = readPageIndex(page);
    pageIndexes.add(previousPageIndex);
    while (page.getOffset() < numBytes) {
      final K currentKey = keyManager.readFromPage(page);
      keys.add(currentKey);
      final int nextPageIndex = readPageIndex(page);
      pageIndexes.add(nextPageIndex);
      @SuppressWarnings("unchecked")
      final int compare = ((Comparable<K>)currentKey).compareTo(key);
      if (compare > 0) {
        final List<Page> childPages = put(previousPageIndex, key, value);
        if (childPages.isEmpty()) {
          return Collections.emptyList();
        } else {
          putChildInteriorPages(childPages, pageIndexes, keys);

          // Add remaining child keys and pages indexes
          while (page.getOffset() < numBytes) {
            final K childKey = keyManager.readFromPage(page);
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

      final Page rightPage = childPages.get(1);
      skipHeader(rightPage);

      return updateOrSplitInteriorPage(page, numBytes, pageIndexes, keys);
    }
  }

  private V removeInterior(final Page page, final K key) {
    V value = null;
    // final List<Integer> pageIndexes = new ArrayList<Integer>();
    // final List<K> keys = new ArrayList<K>();
    // final int numBytes = page.readShort();
    // int previousPageIndex = readPageIndex(page);
    // pageIndexes.add(previousPageIndex);
    // while (page.getOffset() < numBytes) {
    // final K currentKey = keyManager.readFromPage(page);
    //
    // final int nextPageIndex = readPageIndex(page);
    //
    // @SuppressWarnings("unchecked")
    // final int compare = ((Comparable<K>)currentKey).compareTo(key);
    // if (compare > 0) {
    // final Object removeValue = remove(previousPageIndex, key);
    // if (removeValue != null) {
    //
    // // Add remaining child keys and pages indexes
    // while (page.getOffset() < numBytes) {
    // final K childKey = keyManager.readFromPage(page);
    // keys.add(childKey);
    // final int childPageIndex = readPageIndex(page);
    // pageIndexes.add(childPageIndex);
    // }
    //
    // return updateOrSplitInteriorPage(page, numBytes, pageIndexes, keys);
    // }
    // return value;
    // } else {
    // keys.add(currentKey);
    // pageIndexes.add(nextPageIndex);
    // }
    // previousPageIndex = nextPageIndex;
    // }
    // final List<Page> childPages = put(previousPageIndex, key, value);
    // if (childPages.isEmpty()) {
    // return Collections.emptyList();
    // } else {
    // putChildInteriorPages(childPages, pageIndexes, keys);
    //
    // final Page rightPage = childPages.get(1);
    // skipHeader(rightPage);
    //
    // return updateOrSplitInteriorPage(page, numBytes, pageIndexes, keys);
    // }
    // TODO remove
    return value;
  }

  private List<Page> putLeaf(final Page page, final K key, final V value) {
    final List<K> keys = new ArrayList<K>();
    final List<V> values = new ArrayList<V>();

    boolean newValueWritten = false;
    final int numBytes = page.readShort();
    while (page.getOffset() < numBytes) {
      final K currentKey = keyManager.readFromPage(page);
      final V currentValue = valueManager.readFromPage(page);
      @SuppressWarnings("unchecked")
      final int compare = ((Comparable<K>)currentKey).compareTo(key);
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

  private V removeLeaf(final Page page, final K key) {
    V value = null;
    final List<K> keys = new ArrayList<K>();
    final List<V> values = new ArrayList<V>();

    final int numBytes = page.readShort();
    while (page.getOffset() < numBytes) {
      final K currentKey = keyManager.readFromPage(page);
      @SuppressWarnings("unchecked")
      final int compare = ((Comparable<K>)currentKey).compareTo(key);
      if (compare == 0) {
        value = valueManager.removeFromPage(page);
      } else {
        final V currentValue = valueManager.readFromPage(page);
        keys.add(currentKey);
        values.add(currentValue);
      }
    }

    updateOrSplitLeafPage(page, numBytes, keys, values);
    return value;
  }

  public int readPageIndex(final Page page) {
    final int pageIndex = ((Number)pageIndexManager.readFromPage(page)).intValue();
    return pageIndex;
  }

  private void setInteriorIndexAndKeyBytes(
    final Page page,
    final List<byte[]> pageIndexesBytes,
    final List<byte[]> keysBytes,
    final int startIndex,
    final int endIndex) {
    page.setOffset(0);
    page.writeByte(INTERIOR);
    page.writeShort((short)0);
    int i = startIndex;
    for (; i < endIndex; i++) {
      final byte[] pageIndexBytes = pageIndexesBytes.get(i);
      page.writeBytes(pageIndexBytes);
      final byte[] keyBytes = keysBytes.get(i);
      page.writeBytes(keyBytes);
    }
    final byte[] pageIndexBytes = pageIndexesBytes.get(i);
    page.writeBytes(pageIndexBytes);
    setNumBytes(page);
    page.clearBytes(page.getOffset());
    page.flush();
  }

  private void setLeafKeyAndValueBytes(
    final Page page,
    final List<byte[]> keysBytes,
    final List<byte[]> valuesBytes,
    final int startIndex,
    final int endIndex) {
    page.setOffset(0);
    page.writeByte(LEAF);
    page.writeShort((short)0);
    int i = startIndex;
    for (; i < endIndex; i++) {
      final byte[] keyBytes = keysBytes.get(i);
      page.writeBytes(keyBytes);
      final byte[] valueBytes = valuesBytes.get(i);
      page.writeBytes(valueBytes);
    }
    setNumBytes(page);
    page.clearBytes(page.getOffset());
    page.flush();
  }

  private List<Page> updateOrSplitInteriorPage(
    final Page page,
    final int oldNumBytes,
    final List<Integer> pageIndexes,
    final List<K> keys) {
    final List<byte[]> pageIndexesBytes = new ArrayList<byte[]>();
    final List<byte[]> keysBytes = new ArrayList<byte[]>();
    int numBytes = 3;
    final int largeValueIndex = -1;
    int splitIndex = -1;
    int i = 0;
    while (i < keys.size()) {
      final int pageIndex = pageIndexes.get(i);
      final byte[] pageIndexBytes = pageIndexManager.writeToByteArray(pageIndex);
      pageIndexesBytes.add(pageIndexBytes);
      numBytes += pageIndexBytes.length;

      if (i > 0 && splitIndex == -1 && numBytes > minSize) {
        splitIndex = i;
      }
      final K key = keys.get(i);
      final byte[] keyBytes = keyManager.writeToByteArray(key);
      keysBytes.add(keyBytes);
      numBytes += keyBytes.length;

      i++;
    }
    final int pageIndex = pageIndexes.get(i);
    final byte[] pageIndexBytes = pageIndexManager.writeToByteArray(pageIndex);
    pageIndexesBytes.add(pageIndexBytes);
    numBytes += pageIndexBytes.length;

    if (numBytes < page.getSize()) {
      setInteriorIndexAndKeyBytes(page, pageIndexesBytes, keysBytes, 0,
        keysBytes.size());
      return Collections.emptyList();
    } else if (largeValueIndex >= 0) {
      throw new RuntimeException("Splitting not supported");
    } else {
      setInteriorIndexAndKeyBytes(page, pageIndexesBytes, keysBytes, 0,
        splitIndex);
      final Page rightPage = pages.createPage();
      setInteriorIndexAndKeyBytes(rightPage, pageIndexesBytes, keysBytes,
        splitIndex, keysBytes.size());
      return Arrays.asList(page, rightPage);
    }
  }

  private List<Page> updateOrSplitLeafPage(
    final Page page,
    final int oldNumBytes,
    final List<K> keys,
    final List<V> values) {
    final List<byte[]> valuesBytes = new ArrayList<byte[]>();
    final List<byte[]> keysBytes = new ArrayList<byte[]>();
    int numBytes = headerSize;
    final int largeValueIndex = -1;
    int splitIndex = -1;
    int i = 0;
    while (i < keys.size()) {
      final K key = keys.get(i);
      final byte[] keyBytes = keyManager.writeToByteArray(key);
      keysBytes.add(keyBytes);
      numBytes += keyBytes.length;

      final V value = values.get(i);
      final byte[] valueBytes = valueManager.writeToByteArray(value);
      valuesBytes.add(valueBytes);
      numBytes += valueBytes.length;

      i++;
      if (splitIndex == -1 && numBytes > minSize) {
        splitIndex = i;
      }
    }

    if (numBytes < page.getSize()) {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, keysBytes.size());
      return Collections.emptyList();
    } else if (largeValueIndex >= 0) {
      throw new RuntimeException("Splitting not supported");
    } else {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, splitIndex);
      final Page rightPage = pages.createPage();
      setLeafKeyAndValueBytes(rightPage, keysBytes, valuesBytes, splitIndex,
        keysBytes.size());
      return Arrays.asList(page, rightPage);
    }
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public V remove(Object key) {
    return remove(rootPageIndex, (K)key);
  }

  private V remove(int pageIndex, K key) {
    final Page page = pages.getPage(pageIndex);
    page.setOffset(0);
    final byte pageType = page.readByte();
    if (pageType == INTERIOR) {
      return removeInterior(page, key);
    } else if (pageType == LEAF) {
      return removeLeaf(page, key);
    } else {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }
  }
}
