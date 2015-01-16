/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/com.revolsys/trunk/com.revolsys.gis/com.revolsys.gis.core/src/main/java/com/revolsys/gis/data/visitor/Visitor.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2010-01-29 12:04:56 -0800 (Fri, 29 Jan 2010) $
 * $Revision: 2221 $

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
 * See the License for the specific language govercom.revolsys.gis.data.iolimitations under the License.
 */
package com.revolsys.collection;

public interface Visitor<T> {
  /**
   * Visit an item of type T, performing some operation on the item. The method
   * must return true if further items are to be processed or false if no
   * further items are to be processed.
   *
   * @param item The item to process.
   * @return True if further items are to be processed, false otherwise.
   */
  boolean visit(T item);
}
