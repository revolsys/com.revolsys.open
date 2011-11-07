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
package com.revolsys.orm.core;

/**
 * The DataAccessObjectFactory interface defines the methods that factories that
 * return {@link DataAccessObject} implementations for a class must implement.
 * 
 * @author Paul Austin
 */
public interface DataAccessObjectFactory {
  /**
   * Get a DataAcessObject for the specified Class.
   * 
   * @param <T> The DataAccessObject class expected to be returned.
   * @param objectClass The class.
   * @return The data access object for the class.
   */
  <T extends DataAccessObject<?>> T get(
    Class<?> objectClass);

  /**
   * Get a DataAcessObject for the specified class name.
   * 
   * @param <T> The DataAccessObject class expected to be returned.
   * @param objectClassName The class name.
   * @return The data access object for the class name.
   */
  <T extends DataAccessObject<?>> T get(
    String objectClassName);
}
