package com.revolsys.collection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ListResultPager<T> implements ResultPager<T> {
  private final List<T> list = new ArrayList<T>();

  /** The number of objects in a page. */
  private int pageSize = 10;

  /** The current page number. */
  private int pageNumber = -1;

  public ListResultPager(final Collection<? extends T> list) {
    this.list.addAll(list);
  }

  public void close() {
  }

  /**
   * Get the index of the last object in the current page.
   * 
   * @return The index of the last object in the current page.
   */
  public int getEndIndex() {
    final int numPages = getNumPages();
    if (numPages == 0) {
      return 0;
    } else if (pageNumber < numPages - 1) {
      return (pageNumber + 1) * pageSize;
    } else {
      return list.size();
    }
  }

  /**
   * Get the list of objects in the current page.
   * 
   * @return The list of objects in the current page.
   */
  public List<T> getList() {
    if (getNumResults() == 0) {
      return Collections.emptyList();
    } else {
      final int startIndex = getStartIndex() - 1;
      final int endIndex = getEndIndex();
      return list.subList(startIndex, endIndex);
    }
  }

  /**
   * Get the page number of the next page.
   * 
   * @return Thepage number of the next page.
   */
  public int getNextPageNumber() {
    return pageNumber + 2;
  }

  /**
   * Get the number of pages.
   * 
   * @return The number of pages.
   */
  public int getNumPages() {
    return (int)Math.ceil((double)list.size() / getPageSize());
  }

  /**
   * Get the total number of results returned.
   * 
   * @return The total number of results returned.
   */
  public int getNumResults() {
    return list.size();
  }

  /**
   * Get the page number of the current page.
   * 
   * @return Thepage number of the current page.
   */
  public int getPageNumber() {
    return pageNumber + 1;
  }

  /**
   * Get the number of objects to display in a page.
   * 
   * @return The number of objects to display in a page.
   */
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Get the page number of the previous page.
   * 
   * @return Thepage number of the previous page.
   */
  public int getPreviousPageNumber() {
    return pageNumber;
  }

  /**
   * Get the index of the first object in the current page.
   * 
   * @return The index of the first object in the current page.
   */
  public int getStartIndex() {
    final int numPages = getNumPages();
    if (numPages == 0) {
      return 0;
    } else if (pageNumber < numPages - 1) {
      return (pageNumber * pageSize) + 1;
    } else {
      return ((numPages - 1) * pageSize) + 1;
    }
  }

  /**
   * Check to see if there is a next page.
   * 
   * @return True if there is a next page.
   */
  public boolean hasNextPage() {
    return pageNumber < getNumPages();
  }

  /**
   * Check to see if there is a previous page.
   * 
   * @return True if there is a previous page.
   */
  public boolean hasPreviousPage() {
    return pageNumber > 0;
  }

  /**
   * Check to see if this is the first page.
   * 
   * @return True if this is the first page.
   */
  public boolean isFirstPage() {
    return pageNumber == 0;
  }

  /**
   * Check to see if this is the last page.
   * 
   * @return True if this is the last page.
   */
  public boolean isLastPage() {
    return pageNumber == getNumPages();
  }

  /**
   * Set the current page number.
   * 
   * @param pageNumber The current page number.
   */
  public void setPageNumber(final int pageNumber) {
    if (pageNumber - 1 > getNumPages()) {
      this.pageNumber = getNumPages();
    } else if (pageNumber <= 0) {
      this.pageNumber = 0;
    } else {
      this.pageNumber = pageNumber - 1;
    }
  }

  /**
   * Set the number of objects per page.
   * 
   * @param pageSize The number of objects per page.
   */
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
  }
}
