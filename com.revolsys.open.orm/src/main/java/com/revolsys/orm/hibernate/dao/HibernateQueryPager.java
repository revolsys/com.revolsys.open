/*
 * Copyright 2004-2005 Revolution Systems Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.orm.hibernate.dao;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.ScrollableResults;
import org.springframework.orm.hibernate3.SessionFactoryUtils;

import com.revolsys.collection.ResultPager;

/**
 * The HibernateQueryPager is an implementation of {@link ResultPager} that
 * allows paging over a Hibernate {@link Query}.
 * 
 * @author Paul Austin
 */
public class HibernateQueryPager<T> implements ResultPager<T> {
  /** The objects in the current page. */
  private List<T> results;

  /** The number of objects in a page. */
  private int pageSize = 10;

  /** The current page number. */
  private int pageNumber = -1;

  /** The total number of results. */
  private int numResults;

  /** The Hibernate query. */
  private Query query;

  /** The number of pages. */
  private int numPages;

  /**
   * Construct a new HibernateQueryPager.
   * 
   * @param query The Hibernate query.
   */
  public HibernateQueryPager(final Query query) {
    this.query = query;
    final ScrollableResults scrollableResults = query.scroll();
    scrollableResults.last();
    this.numResults = scrollableResults.getRowNumber() + 1;
  }

  /**
   * Construct a new HibernateQueryPager.
   * 
   * @param query The Hibernate query.
   * @param pageNumber The current page number.
   * @param pageSize The number of objects per page.
   */
  public HibernateQueryPager(final Query query, final int pageNumber,
    final int pageSize) {
    this(query);
    setPageSize(pageSize);
    setPageNumber(pageNumber);
  }

  @Override
  public void close() {
  }

  /**
   * Get the index of the last object in the current page.
   * 
   * @return The index of the last object in the current page.
   */
  @Override
  public int getEndIndex() {
    if (pageNumber == numPages) {
      return numResults;
    } else {
      return (pageNumber + 1) * pageSize;
    }
  }

  /**
   * Get the list of objects in the current page.
   * 
   * @return The list of objects in the current page.
   */
  @Override
  public List<T> getList() {
    if (results == null) {
      throw new IllegalStateException(
        "The page number must be set using setPageNumber");
    }
    return results;
  }

  /**
   * Get the page number of the next page.
   * 
   * @return Thepage number of the next page.
   */
  @Override
  public int getNextPageNumber() {
    return pageNumber + 2;
  }

  /**
   * Get the number of pages.
   * 
   * @return The number of pages.
   */
  @Override
  public int getNumPages() {
    return numPages + 1;
  }

  /**
   * Get the total number of results returned.
   * 
   * @return The total number of results returned.
   */
  @Override
  public int getNumResults() {
    return numResults;
  }

  /**
   * Get the page number of the current page.
   * 
   * @return Thepage number of the current page.
   */
  @Override
  public int getPageNumber() {
    return pageNumber + 1;
  }

  /**
   * Get the number of objects to display in a page.
   * 
   * @return The number of objects to display in a page.
   */
  @Override
  public int getPageSize() {
    return pageSize;
  }

  /**
   * Get the page number of the previous page.
   * 
   * @return Thepage number of the previous page.
   */
  @Override
  public int getPreviousPageNumber() {
    return pageNumber;
  }

  /**
   * Get the index of the first object in the current page.
   * 
   * @return The index of the first object in the current page.
   */
  @Override
  public int getStartIndex() {
    return (pageNumber * pageSize) + 1;
  }

  /**
   * Check to see if there is a next page.
   * 
   * @return True if there is a next page.
   */
  @Override
  public boolean hasNextPage() {
    return pageNumber < numPages;
  }

  /**
   * Check to see if there is a previous page.
   * 
   * @return True if there is a previous page.
   */
  @Override
  public boolean hasPreviousPage() {
    return pageNumber > 0;
  }

  /**
   * Check to see if this is the first page.
   * 
   * @return True if this is the first page.
   */
  @Override
  public boolean isFirstPage() {
    return pageNumber == 0;
  }

  /**
   * Check to see if this is the last page.
   * 
   * @return True if this is the last page.
   */
  @Override
  public boolean isLastPage() {
    return pageNumber == numPages;
  }

  /**
   * Set the current page number.
   * 
   * @param pageNumber The current page number.
   */
  @Override
  public void setPageNumber(final int pageNumber) {
    if (pageNumber - 1 > numPages) {
      this.pageNumber = numPages;
    } else if (pageNumber <= 0) {
      this.pageNumber = 0;
    } else {
      this.pageNumber = pageNumber - 1;
    }
    updateResults();
  }

  @Override
  public void setPageNumberAndSize(final int pageSize, final int pageNumber) {
    setPageSize(pageSize);
    setPageNumber(pageNumber);
  }

  /**
   * Set the number of objects per page.
   * 
   * @param pageSize The number of objects per page.
   */
  @Override
  public void setPageSize(final int pageSize) {
    this.pageSize = pageSize;
    this.numPages = Math.max(0, ((numResults - 1) / pageSize));
    updateResults();
  }

  /**
   * Update the cached results for the current page.
   */
  private void updateResults() {
    if (pageNumber != -1) {
      try {
        query.setFirstResult(pageNumber * pageSize);
        query.setMaxResults(pageSize);
        this.results = query.list();
      } catch (final HibernateException e) {
        throw SessionFactoryUtils.convertHibernateAccessException(e);
      }
    }
  }
}
