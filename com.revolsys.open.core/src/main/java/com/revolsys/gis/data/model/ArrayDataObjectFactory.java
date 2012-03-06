/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

 * Copyright 2004-2007 Revolution Systems Inc.
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
package com.revolsys.gis.data.model;

import java.lang.ref.WeakReference;

/**
 * The ArrayDataObjectFactory is an implementation of {@link DataObjectFactory}
 * for creating {@link ArrayDataObject} instances.
 * 
 * @author Paul Austin
 * @see ArrayDataObject
 */
public class ArrayDataObjectFactory implements DataObjectFactory {

  private static WeakReference<ArrayDataObjectFactory> instance;

  /**
   * Get the instance of the factory.
   * 
   * @return The instance.
   */
  public static synchronized ArrayDataObjectFactory getInstance() {
    ArrayDataObjectFactory factory = null;
    if (instance != null) {
      factory = instance.get();
    }
    if (factory == null) {
      factory = new ArrayDataObjectFactory();
      instance = new WeakReference<ArrayDataObjectFactory>(factory);
    }
    return factory;
  }

  /**
   * Create an instance of ArrayDataObject using the metadata
   * 
   * @param metaData The metadata used to create the instance.
   * @return The DataObject instance.
   */
  public ArrayDataObject createDataObject(final DataObjectMetaData metaData) {
    return new ArrayDataObject(metaData);
  }
}
