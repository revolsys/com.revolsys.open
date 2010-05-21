/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/shape/io/MoepDirectoryReader.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-06-09 09:28:28 -0700 (Sat, 09 Jun 2007) $
 * $Revision:265 $

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
package com.revolsys.gis.format.shape.io;

import java.io.File;
import java.io.FilenameFilter;

import com.revolsys.gis.data.io.AbstractDirectoryReader;
import com.revolsys.gis.data.io.DataObjectDirectoryReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.io.filter.ExtensionFilenameFilter;

/**
 * <p>
 * The MoepDirectoryReader is a {@link DataObjectReader} that can read .shp data
 * files contained in a single directory. The reader will iterate through the
 * .shp files in alpabetical order returning all features.
 * </p>
 * <p>
 * See the {@link AbstractDirectoryReader} class for examples on how to use
 * dataset readers.
 * </p>
 * 
 * @author Paul Austin
 * @see AbstractDirectoryReader
 */
public class ShapeDirectoryReader extends DataObjectDirectoryReader {
  /** A filename filter matching .dbf files. */
  private static final FilenameFilter SHP_FILTER = new ExtensionFilenameFilter(
    "shp");

  /**
   * Construct a new ShapeDirectoryReader.
   * 
   * @param directory The containing the .shp files.
   */
  public ShapeDirectoryReader(
    final File directory) {
    setFileExtensions("shp");
    setDirectory(directory);
  }
}
