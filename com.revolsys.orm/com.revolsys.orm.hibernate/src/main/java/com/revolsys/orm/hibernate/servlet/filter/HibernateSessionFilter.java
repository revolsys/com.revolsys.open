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
package com.revolsys.orm.hibernate.servlet.filter;

import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.dao.CleanupFailureDataAccessException;
import org.springframework.orm.hibernate3.support.OpenSessionInViewFilter;

/**
 * The HibernateSessionFilter is a servlet filter that ensures a Hibernate
 * Session is open for the duration of a HTTP request and that the session is
 * flushed at the end of the session.
 * 
 * @author Paul Austin
 */
public class HibernateSessionFilter extends OpenSessionInViewFilter {
  /**
   * Get the session from the session factory with a flush mode of AUTO.
   * 
   * @param sessionFactory The factory used to create the session.
   * @return The session.
   */
  protected Session getSession(final SessionFactory sessionFactory) {
    Session session = super.getSession(sessionFactory);
    session.setFlushMode(FlushMode.AUTO);
    return session;
  }

  /**
   * Close and flush the session after the request.
   * 
   * @param session The session to close.
   * @param sessionFactory The sesstion factory for the session.
   */
  protected void closeSession(final Session session,
    final SessionFactory sessionFactory) {
    try {
      session.flush();
    } catch (HibernateException e) {
      throw new CleanupFailureDataAccessException(e.getMessage(), e);
    } finally {
      super.closeSession(session, sessionFactory);
    }
  }
}
