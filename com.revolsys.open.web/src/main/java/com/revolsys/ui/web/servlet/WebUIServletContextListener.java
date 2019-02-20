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
package com.revolsys.ui.web.servlet;

import java.beans.Introspector;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.logging.LogFactory;

/**
 * @author paustin
 * @version 1.0
 */
public class WebUIServletContextListener implements ServletContextListener {

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.
   * ServletContextEvent)
   */
  @Override
  public final void contextDestroyed(final ServletContextEvent event) {
    final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    LogFactory.releaseAll();
    Introspector.flushCaches();
  }

  /*
   * (non-Javadoc)
   * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.
   * ServletContextEvent)
   */
  @Override
  public final void contextInitialized(final ServletContextEvent event) {
  }

}
