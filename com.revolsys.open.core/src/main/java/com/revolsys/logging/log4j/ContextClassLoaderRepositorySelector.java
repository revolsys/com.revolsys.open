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
package com.revolsys.logging.log4j;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.RepositorySelector;
import org.apache.log4j.spi.RootLogger;

/**
 * The ContextClassLoaderRepositorySelector class is used to manage different
 * {@link LoggerRepository} heirarchies for different context class loaders. The
 * {@link #add()} method can be used to create a seperate logger repository for
 * the current thread context class loader. When the class loader is about to be
 * destroyed use the {@link #remove()} method to clean up the logger repository
 * for the class loader. See the Log4jServletContextListener for use in web
 * applications.
 *
 * @author Paul Austin
 * @version 1.0
 */
public class ContextClassLoaderRepositorySelector implements RepositorySelector {
  /**
   * Add a new hierarchy for the current thread context class loader if one does
   * not exist or return the previous hierarchy.
   *
   * @return The created heirarchy.
   */
  public static synchronized Hierarchy add() {
    final Thread thread = Thread.currentThread();
    final ClassLoader classLoader = thread.getContextClassLoader();
    return add(classLoader);
  }

  /**
   * Add a new hierarchy for the specified class loader if one does not exist or
   * return the previous hierarchy.
   *
   * @param classLoader The classloader to create the hierarchy for.
   * @return The created heirarchy.
   */
  public static synchronized Hierarchy add(final ClassLoader classLoader) {
    Hierarchy hierarchy = (Hierarchy)repositories.get(classLoader);
    if (hierarchy == null) {
      hierarchy = new Hierarchy(new RootLogger(Level.DEBUG));
      repositories.put(classLoader, hierarchy);
    }
    return hierarchy;
  }

  /**
   * Remove and shutdown the hierarchy for the current thread context class
   * loader class loader.
   */
  public static synchronized void remove() {
    final Thread thread = Thread.currentThread();
    final ClassLoader classLoader = thread.getContextClassLoader();
    remove(classLoader);
  }

  /**
   * Remove and shutdown the hierarchy for the specified class loader.
   *
   * @param classLoader The classloader to create the hierarchy for.
   */
  public static synchronized void remove(final ClassLoader classLoader) {
    final Hierarchy hierarchy = (Hierarchy)repositories.remove(classLoader);
    if (hierarchy != null) {
      hierarchy.shutdown();
    }
  }

  /**
   * The deault repository to use when one hasn't been created for the class
   * loader.
   */
  private static final LoggerRepository defaultRepository;

  /** The gaurd used to set the respository selector on the LogManager. */
  private static final Object GUARD;

  /** The map of class loaders to logging hierarchies. */
  private static final Map repositories = new HashMap();

  static {
    try {
      GUARD = LogManager.getRootLogger();
      defaultRepository = LogManager.getLoggerRepository();
      final RepositorySelector selector = new ContextClassLoaderRepositorySelector();
      LogManager.setRepositorySelector(selector, GUARD);
    } catch (final RuntimeException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Get the logger repository for the current thread context class loader or
   * the default one if one does not exist.
   *
   * @return The logger repository.
   */
  @Override
  public final synchronized LoggerRepository getLoggerRepository() {
    final Thread thread = Thread.currentThread();
    final ClassLoader classLoader = thread.getContextClassLoader();
    final Hierarchy hierarchy = (Hierarchy)repositories.get(classLoader);
    if (hierarchy == null) {
      return defaultRepository;
    } else {
      return hierarchy;
    }
  }

}
