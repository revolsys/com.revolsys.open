/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/rs-gis-dbase/trunk/src/main/java/com/revolsys/gis/format/xbase/io/XbaseDatasetReader.java $
 * $Author:paul.austin@revolsys.com $
 * $Date:2007-07-03 19:26:54 -0700 (Tue, 03 Jul 2007) $
 * $Revision:410 $

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
package com.revolsys.gis.format.xbase.io;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.revolsys.gis.data.io.AbstractFileDatasetDataObjectReader;
import com.revolsys.gis.data.io.DataObjectReader;
import com.revolsys.io.filter.ExtensionFilenameFilter;

/**
 * <p>
 * The XbaseDatasetReader is a {@link DataObjectReader} that can read .dbf data
 * files contained either in a single .zip file or in a single directory. The
 * reader will iterate through the .dbf files in alpabetical order returning all
 * features.
 * </p>
 * <p>
 * See the {@link AbstractFileDatasetDataObjectReader} class for examples on how
 * to use dataset readers.
 * </p>
 * 
 * @author Paul Austin
 * @see AbstractFileDatasetDataObjectReader
 */
public class XbaseDatasetReader extends AbstractFileDatasetDataObjectReader {
  /** A filename filter matching .dbf files. */
  private static final FilenameFilter DBF_FILTER = new ExtensionFilenameFilter(
    "dbf");

  /**
   * Constuct a new XbaseDatasetReader to read .dbf files from the specified
   * directory or .zip file.
   * 
   * @param file The directory or .zip file containing the .dbf files.
   * @throws IOException If an I/O errror occurs.
   */
  public XbaseDatasetReader(
    final File file)
    throws IOException {
    super(file);
  }

  /**
   * Create a new {@link XbaseFileReader} to read the file.
   * 
   * @param file The file to read.
   * @return The reader for the file.
   * @throws IOException If an I/O error occurs.
   */
  @Override
  protected DataObjectReader createFileDataObjectReader(
    final File file)
    throws IOException {
    return new XbaseFileReader(file);
  }

  /**
   * Get the list of .dbf files in the dataset.
   * 
   * @return The list of files.
   */
  @Override
  protected List<File> getFiles() {
    return Arrays.asList(getWorkingDirectory().listFiles(DBF_FILTER));
  }

}
