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
package com.revolsys.orm.hibernate.callback;

import java.sql.SQLException;
import java.util.Collection;

import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * The HibernateFilterCallback is designed to execute the hibernate
 * {@link Session#createFilter(java.lang.Object, java.lang.String)} method
 * within a spring framework
 * {@link org.springframework.orm.hibernate3.HibernateTemplate#execute(org.springframework.orm.hibernate3.HibernateCallback)}
 * method.
 * 
 * @author Paul Austin
 */
public class HibernateFilterCallback implements HibernateCallback {
  /** The collection to filter. */
  private Collection collection;

  /** The filter to apply to the collection. */
  private String filter;

  /**
   * Construct a new HibernateFilterCallback.
   * 
   * @param collection The collection to filter.
   * @param filter The filter to apply to the collection.
   */
  public HibernateFilterCallback(final Collection collection,
    final String filter) {
    super();
    this.collection = collection;
    this.filter = filter;
  }

  /**
   * Peform the action.
   * 
   * @param session The hibernate session.
   * @return The result of the action.
   * @throws SQLException If a SQL exception occurs.
   */
  public Object doInHibernate(final Session session) throws SQLException {
    return session.createFilter(collection, filter);
  }

}
