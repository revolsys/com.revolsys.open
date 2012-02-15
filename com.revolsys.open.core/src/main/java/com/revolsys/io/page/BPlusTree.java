package com.revolsys.io.page;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BPlusTree<K, V> extends AbstractMap<K, V> {
  private class PutResult {
    private V oldValue;

    private byte[] newKeyBytes;

    private byte[] newPageIndexBytes;

    public void clear() {
      newKeyBytes = null;
      newPageIndexBytes = null;
    }

    public boolean wasSplit() {
      return newKeyBytes != null;
    }
  }

  private class RemoveResult {
    private V oldValue;

    public boolean canMerge() {
      return false;
    }

    public void clear() {
    }
  }

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
      pages.releasePage(rootPage);
    }
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    // TODO Auto-generated method stub
    return null;
  }

  protected V get(final int pageIndex, final K key) {
    V result;
    final Page page = pages.getPage(pageIndex);
    page.setOffset(0);
    final byte pageType = page.readByte();
    if (pageType == INTERIOR) {
      result = getInterior(page, key);
    } else if (pageType == LEAF) {
      result = getLeaf(page, key);
    } else {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }
    pages.releasePage(page);
    return result;
  }

  @Override
  @SuppressWarnings("unchecked")
  public V get(final Object key) {
    return get(rootPageIndex, (K)key);
  }

  @SuppressWarnings("unchecked")
  private V getInterior(final Page page, final K key) {
    final int numBytes = page.readShort();
    final int pageIndex = page.readInt();
    int previousPageIndex = pageIndex;
    while (page.getOffset() < numBytes) {
      final K currentKey = keyManager.readFromPage(page);
      final int nextPageIndex = page.readInt();
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
    try {
      final List<Integer> pageIndexes = new ArrayList<Integer>();
      final int offset = page.getOffset();
      page.setOffset(0);
      final byte pageType = page.readByte();
      final int numBytes = page.readShort();
      if (pageType == INTERIOR) {
        final int pageIndex1 = page.readInt();
        int childPageIndex = pageIndex1;
        pageIndexes.add(childPageIndex);
        System.out.print("I");
        System.out.print(page.getIndex());
        System.out.print("\t");
        System.out.print(numBytes);
        System.out.print("\t");
        System.out.print(pageIndex1);
        while (page.getOffset() < numBytes) {
          final K value = keyManager.readFromPage(page);
          final int pageIndex2 = page.readInt();
          childPageIndex = pageIndex2;
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
    } finally {
      pages.releasePage(page);
    }
  }

  protected PutResult put(
    final int pageIndex,
    final Integer nextPageIndex,
    final K key,
    final V value) {
    PutResult result;
    final Page page = pages.getPage(pageIndex);
    page.setOffset(0);
    final byte pageType = page.readByte();
    if (pageType == INTERIOR) {
      result = putInterior(page, key, value);
    } else if (pageType == LEAF) {
      result = putLeaf(page, nextPageIndex, key, value);
    } else {
      throw new IllegalArgumentException("Unknown page type " + pageType);
    }
    pages.releasePage(page);
    return result;
  }

  @Override
  public V put(final K key, final V value) {
    final PutResult result = put(rootPageIndex, null, key, value);
    if (result.wasSplit()) {
      final Page rootPage = pages.getPage(rootPageIndex);
      final Page leftPage = pages.createPage();
      leftPage.setContent(rootPage);

      rootPage.clear();
      final byte pageType = INTERIOR;
      writePageHeader(rootPage, pageType);

      final int firstChildPageIndex = leftPage.getIndex();
      rootPage.writeInt(firstChildPageIndex);

      final byte[] keyBytes = result.newKeyBytes;
      rootPage.writeBytes(keyBytes);

      rootPage.writeBytes(result.newPageIndexBytes);
      setNumBytes(rootPage);
      pages.releasePage(rootPage);
      pages.releasePage(leftPage);
    }
    return result.oldValue;
  }

  private PutResult putInterior(final Page page, final K key, final V value) {
    PutResult result = null;
    final List<byte[]> pageIndexesBytes = new ArrayList<byte[]>();
    final List<byte[]> keysBytes = new ArrayList<byte[]>();
    final int numBytes = page.readShort();
    final byte[] pageIndexBytes = MethodPageValueManager.getIntBytes(page);
    byte[] previousPageIndexBytes = pageIndexBytes;
    pageIndexesBytes.add(previousPageIndexBytes);
    while (page.getOffset() < numBytes) {
      final byte[] currentKeyBytes = keyManager.getBytes(page);
      final K currentKey = keyManager.getValue(currentKeyBytes);
      final byte[] nextPageIndexBytes = MethodPageValueManager.getIntBytes(page);
      if (result == null) {
        @SuppressWarnings("unchecked")
        final int compare = ((Comparable<K>)currentKey).compareTo(key);
        if (compare > 0) {
          final int previousPageIndex = MethodPageValueManager.getIntValue(previousPageIndexBytes);
          final int nextPageIndex = MethodPageValueManager.getIntValue(nextPageIndexBytes);
          result = put(previousPageIndex, nextPageIndex, key, value);
          if (result.wasSplit()) {
            pageIndexesBytes.add(result.newPageIndexBytes);
            keysBytes.add(result.newKeyBytes);
          } else {
            return result;
          }
        }
      }
      keysBytes.add(currentKeyBytes);
      pageIndexesBytes.add(nextPageIndexBytes);
      previousPageIndexBytes = nextPageIndexBytes;
    }
    if (result == null) {
      final int previousPageIndex = MethodPageValueManager.getIntValue(previousPageIndexBytes);
      result = put(previousPageIndex, null, key, value);
      if (result.wasSplit()) {
        pageIndexesBytes.add(result.newPageIndexBytes);
        keysBytes.add(result.newKeyBytes);
      } else {
        return result;
      }
    }
    updateOrSplitInteriorPage(result, page, keysBytes, pageIndexesBytes);
    return result;
  }

  private PutResult putLeaf(
    final Page page,
    final Integer nextPageIndex,
    final K key,
    final V value) {
    final PutResult result = new PutResult();
    final byte[] keyBytes = keyManager.getBytes(key);
    final List<byte[]> keysBytes = new ArrayList<byte[]>();
    final List<byte[]> valuesBytes = new ArrayList<byte[]>();
    final byte[] valueBytes = valueManager.getBytes(value);

    boolean newValueWritten = false;
    final int numBytes = page.readShort();
    while (page.getOffset() < numBytes) {
      final byte[] currentKeyBytes = keyManager.getBytes(page);
      final K currentKey = keyManager.getValue(currentKeyBytes);
      final byte[] currentValueBytes = valueManager.getBytes(page);
      @SuppressWarnings("unchecked")
      final int compare = ((Comparable<K>)currentKey).compareTo(key);

      if (compare >= 0) {
        keysBytes.add(keyBytes);
        valuesBytes.add(valueBytes);
        newValueWritten = true;
      }
      if (compare == 0) {
        result.oldValue = valueManager.getValue(currentValueBytes);
      } else {
        keysBytes.add(currentKeyBytes);
        valuesBytes.add(currentValueBytes);
      }

    }
    if (!newValueWritten) {
      keysBytes.add(keyBytes);
      valuesBytes.add(valueBytes);
    }
    updateOrSplitLeafPage(result, page, numBytes, keysBytes, valuesBytes);
    return result;
  }

  private RemoveResult remove(final int pageIndex, final K key) {
    final Page page = pages.getPage(pageIndex);
    try {
      page.setOffset(0);
      final byte pageType = page.readByte();
      if (pageType == INTERIOR) {
        return removeInterior(page, key);
      } else if (pageType == LEAF) {
        return removeLeaf(page, key);
      } else {
        throw new IllegalArgumentException("Unknown page type " + pageType);
      }
    } finally {
      pages.releasePage(page);
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public V remove(final Object key) {
    final RemoveResult result = remove(rootPageIndex, (K)key);
    // TODO merge if required
    return result.oldValue;
  }

  private RemoveResult removeInterior(final Page page, final K key) {
    final int numBytes = page.readShort();
    final int pageIndex = page.readInt();
    int previousPageIndex = pageIndex;
    while (page.getOffset() < numBytes) {
      final K currentKey = keyManager.readFromPage(page);
      final int nextPageIndex = page.readInt();
      final int compare = ((Comparable<K>)currentKey).compareTo(key);
      if (compare > 0) {
        return remove(previousPageIndex, key);
      }
      previousPageIndex = nextPageIndex;
    }
    return remove(previousPageIndex, key);
  }

  private RemoveResult removeLeaf(final Page page, final K key) {
    final RemoveResult result = new RemoveResult();
    final List<byte[]> keysBytes = new ArrayList<byte[]>();
    final List<byte[]> valuesBytes = new ArrayList<byte[]>();

    final int numBytes = page.readShort();
    while (page.getOffset() < numBytes) {
      final byte[] keyBytes = keyManager.getBytes(page);
      final byte[] valueBytes = valueManager.getBytes(page);
      if (result.oldValue == null) {
        final K currentKey = keyManager.getValue(keyBytes);
        final int compare = ((Comparable<K>)currentKey).compareTo(key);
        if (compare == 0) {
          result.oldValue = valueManager.getValue(valueBytes);
        } else {
          keysBytes.add(keyBytes);
          valuesBytes.add(valueBytes);
        }
      } else {
        keysBytes.add(keyBytes);
        valuesBytes.add(valueBytes);
      }
    }
    if (result.oldValue != null) {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, keysBytes.size());
    }
    // TODO size
    return result;
  }

  private void setInteriorKeyAndValueBytes(
    final Page page,
    final List<byte[]> keysBytes,
    final List<byte[]> pageIndexesBytes,
    final int startIndex,
    final int endIndex) {
    page.setOffset(0);
    page.writeByte(INTERIOR);
    page.writeShort((short)0);
    int i = startIndex;
    writeBytes(page, pageIndexesBytes, i);
    for (; i < endIndex; i++) {
      writeBytes(page, keysBytes, i);
      writeBytes(page, pageIndexesBytes, i + 1);
    }
    setNumBytes(page);
    page.clearBytes(page.getOffset());
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
      writeBytes(page, keysBytes, i);
      writeBytes(page, valuesBytes, i);
    }
    setNumBytes(page);
    page.clearBytes(page.getOffset());
  }

  private void updateOrSplitInteriorPage(
    final PutResult result,
    final Page page,
    final List<byte[]> keysBytes,
    final List<byte[]> pageIndexBytes) {
    result.clear();
    int numBytes = headerSize;
    int splitIndex = -1;
    int i = 0;
    numBytes += pageIndexBytes.get(0).length;
    while (i < keysBytes.size()) {
      numBytes += keysBytes.get(i).length;
      numBytes += pageIndexBytes.get(i + 1).length;

      i++;
      if (splitIndex == -1 && numBytes > minSize) {
        splitIndex = i;
      }
    }

    if (numBytes < page.getSize()) {
      setInteriorKeyAndValueBytes(page, keysBytes, pageIndexBytes, 0,
        keysBytes.size());
    } else {
      setInteriorKeyAndValueBytes(page, keysBytes, pageIndexBytes, 0,
        splitIndex);
      final Page rightPage = pages.createPage();
      setInteriorKeyAndValueBytes(rightPage, keysBytes, pageIndexBytes,
        splitIndex, keysBytes.size());

      result.newPageIndexBytes = MethodPageValueManager.getValueIntBytes(rightPage.getIndex());
      result.newKeyBytes = keysBytes.get(splitIndex);
      pages.releasePage(rightPage);
    }
  }

  private void updateOrSplitLeafPage(
    final PutResult result,
    final Page page,
    final int oldNumBytes,
    final List<byte[]> keysBytes,
    final List<byte[]> valuesBytes) {
    int numBytes = headerSize;
    int splitIndex = -1;
    int i = 0;
    while (i < keysBytes.size()) {
      final byte[] keyBytes = keysBytes.get(i);
      numBytes += keyBytes.length;

      final byte[] valueBytes = valuesBytes.get(i);
      numBytes += valueBytes.length;

      i++;
      if (splitIndex == -1 && numBytes > minSize) {
        splitIndex = i;
      }
    }

    if (numBytes < page.getSize()) {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, keysBytes.size());
    } else {
      setLeafKeyAndValueBytes(page, keysBytes, valuesBytes, 0, splitIndex);
      final Page rightPage = pages.createPage();
      setLeafKeyAndValueBytes(rightPage, keysBytes, valuesBytes, splitIndex,
        keysBytes.size());

      result.newPageIndexBytes = MethodPageValueManager.getValueIntBytes(rightPage.getIndex());
      result.newKeyBytes = keysBytes.get(splitIndex);
      pages.releasePage(rightPage);
    }
  }

  public void writeBytes(
    final Page page,
    final List<byte[]> bytesList,
    final int i) {
    final byte[] pageIndexBytes = bytesList.get(i);
    page.writeBytes(pageIndexBytes);
  }
}
