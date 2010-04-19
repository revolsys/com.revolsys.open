/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/com.revolsys.gis/trunk/com.revolsys.gis.core/src/main/java/com/revolsys/gis/io/Writer<DataObject>.java $
 * $Author: $
 * $Date: $
 * $Revision: -1 $

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
 * See the License for the specifcom.revolsys.gis.format.core.ions and
 * limitations under the License.
 */
package com.revolsys.io;

public interface Writer<T> extends ObjectWithProperties {
  void close();

  void flush();

  void write(
    T object);
}
