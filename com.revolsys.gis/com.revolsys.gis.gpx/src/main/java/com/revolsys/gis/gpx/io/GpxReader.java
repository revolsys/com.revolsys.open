/*
 * $URL: https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/java/com/revolsys/gis/format/xbase/io/XbaseFileReader.java $
 * $Author: paul.austin@revolsys.com $
 * $Date: 2006-01-31 15:41:41 -0800 (Tue, 31 Jan 2006) $
 * $Revision: 76 $

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
package com.revolsys.gis.gpx.io;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Iterator;

import javax.xml.namespace.QName;

import com.revolsys.gis.cs.epsg.EpsgCoordinateSystems;
import com.revolsys.gis.data.io.AbstractReader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;

public class GpxReader extends AbstractReader<DataObject> {

  private final DataObjectFactory dataObjectFactory;

  private final Reader in;

  private final GpxIterator iterator;

  private QName typeName;

  public GpxReader(
    final InputStream in,
    final DataObjectFactory dataObjectFactory) {
    this(new InputStreamReader(in), dataObjectFactory);
  }

  public GpxReader(
    final Reader in,
    final DataObjectFactory dataObjectFactory) {
    this.in = in;
    this.dataObjectFactory = dataObjectFactory;
    this.iterator = new GpxIterator(in, dataObjectFactory, typeName);
    setProperty(IoConstants.COORDINATE_SYSTEM_PROPERTY,
      EpsgCoordinateSystems.getCoordinateSystem(4326));

  }

  public void close() {
    FileUtil.closeSilent(in);
  }

  public Iterator iterator() {
    return iterator;
  }
}
