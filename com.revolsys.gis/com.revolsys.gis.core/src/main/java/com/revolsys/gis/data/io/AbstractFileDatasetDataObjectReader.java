/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/core/io/AbstractFileDatasetDataObjectReader.java $
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
package com.revolsys.gis.data.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.apache.log4j.Logger;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.FileUtil;
import com.revolsys.io.ZipUtil;

public abstract class AbstractFileDatasetDataObjectReader extends
  AbstractReader implements Iterator<DataObject> {
  /** The current file being processed. */
  private File currentFile;

  private Iterator<DataObject> currentIterator;

  /** The reader for the current file being processed. */
  private DataObjectReader currentReader;

  /** The file of data to read from. */
  private File file;

  /** The files to be read by this reader. */
  private Iterator<File> fileIterator;

  /** The files to be read by this reader. */
  private List<File> files;

  /** Flag indicating if the reader has more objects to be read. */
  private boolean hasNext = true;

  /** The logging instance. */
  private final Logger log = Logger.getLogger(getClass());

  /** The type names to use instead of the file name prefix. */
  private Map<String, String> typeNameAliases = new HashMap<String, String>();

  /** The working directory to read data (maybe unzippped) from. */
  private File workingDirectory;

  public AbstractFileDatasetDataObjectReader() {
  }

  /**
   * Construct a new AbstractFileDatasetDataObjectReader to read files from the
   * specified directory or .zip file.
   * 
   * @param file The directory or .zip file containing the files.
   * @throws IOException If an I/O error occurs.
   */
  public AbstractFileDatasetDataObjectReader(
    final File file)
    throws IOException {
    setFile(file);
  }

  /**
   * Close the connection to the data store.
   */
  public void close() {
    if (log.isInfoEnabled()) {
      log.info("Closing dataset '" + file + "'");
    }
    if (currentReader != null) {
      try {
        currentReader.close();
      } catch (final Throwable e) {
        log.error("  Error closing reader " + e.getMessage(), e);

      }
    }
    if (!file.isDirectory()) {
      if (log.isDebugEnabled()) {
        log.debug("  Deleting working files");
      }
      try {
        FileUtil.deleteDirectory(workingDirectory);
      } catch (final IOException e) {
        log.error("  Error deleting working directory " + e.getMessage(), e);
      }
    }
    if (log.isDebugEnabled()) {
      log.debug("  Finished closing file");
    }
  }

  /**
   * Create a new {@link DataObjectReader} to read the file.
   * 
   * @param file The file to read.
   * @return The reader for the file.
   * @throws IOException If an I/O error occurs.
   */
  protected abstract DataObjectReader createFileDataObjectReader(
    File file)
    throws IOException;

  public File getFile() {
    return file;
  }

  /**
   * Get the files that are to be read by this reader. This must be overwritten
   * in sub classes to return the files in the working directory that are to be
   * read by instances of the write returned by
   * {@link #createFileDataObjectReader(File)}.
   * 
   * @return The list of files.
   */
  protected abstract List<File> getFiles();

  /**
   * Get the type name alias for the file name prefix from the specified file.
   * 
   * @param file The file.
   * @return The type name alias;
   */
  public String getTypeNameAlias(
    final File file) {
    final String prefix = FileUtil.getFileNamePrefix(file);
    return getTypeNameAlias(prefix);
  }

  /**
   * Get the type name alias for the type name.
   * 
   * @param typeName The type name.
   * @return The type name alias;
   */
  public String getTypeNameAlias(
    final String typeName) {
    final String alias = typeNameAliases.get(typeName);
    if (alias == null) {
      return typeName;
    }
    return alias;
  }

  /**
   * Get the type names to use instead of the file name prefix.
   * 
   * @return The type names to use instead of the file name prefix.
   */
  public Map<String, String> getTypeNameAliases() {
    return typeNameAliases;
  }

  /**
   * Get the working directory for the reader.
   * 
   * @return The working directory.
   */
  protected File getWorkingDirectory() {
    return workingDirectory;
  }

  /**
   * Check to see if the reader has more data objects to be read.
   * 
   * @return True if the reader has more data objects to be read.
   */
  public boolean hasNext() {
    if (!hasNext) {
      return false;
    } else if (currentIterator == null || !currentIterator.hasNext()) {
      do {
        if (fileIterator.hasNext()) {
          if (currentReader != null) {
            try {
              if (log.isInfoEnabled()) {
                log.info("Closing " + currentFile.getName());
              }
              currentReader.close();
            } catch (final Throwable t) {
              log.warn(t.getMessage(), t);
            }
          }
          currentFile = fileIterator.next();
          try {
            currentReader = createFileDataObjectReader(currentFile);
            if (currentReader != null) {
              currentIterator = currentReader.iterator();
            } else {
              currentIterator = null;
            }
            return true;
          } catch (final IOException e) {
            log.error(e.getMessage(), e);
          }

        } else {
          hasNext = false;
          return false;
        }
      } while (!currentIterator.hasNext());
      return true;
    } else {
      return true;
    }
  }

  protected void init()
    throws IOException {
  }

  public Iterator<DataObject> iterator() {
    return this;
  }

  /**
   * Get the next data object read by this reader.
   * 
   * @return The next DataObject.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  public DataObject next() {
    if (hasNext()) {
      return currentIterator.next();
    } else {
      throw new NoSuchElementException();
    }
  }

  /**
   * Open the dataset. If the dataset is a directory, set the working diretcory
   * to that directory otherwise create a new temp working directory and extract
   * the zip archive.
   * 
   * @throws IOException If an I/O error occurs.
   */
  private void openDataset()
    throws IOException {
    if (log.isInfoEnabled()) {
      log.info("Opening dataset '" + file + "'");
    }
    if (!file.exists()) {
      throw new FileNotFoundException(file.getAbsolutePath());
    } else if (file.isDirectory()) {
      workingDirectory = file;
    } else {
      final String filePrefix = file.getName();
      workingDirectory = FileUtil.createTempDirectory(filePrefix, ".tmp");
      FileUtil.deleteFileOnExit(workingDirectory);
      if (log.isDebugEnabled()) {
        log.debug("  Unzipping archive");
      }
      ZipUtil.unzipFile(file, workingDirectory);
    }
    if (log.isDebugEnabled()) {
      log.debug("  Finished opening dataset");
    }
    init();
  }

  /**
   * Removing data objects is not supported.
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }

  public void setFile(
    final File file)
    throws IOException {
    this.file = file.getCanonicalFile();
    openDataset();

    files = getFiles();
    Collections.sort(files);
    fileIterator = files.iterator();
  }

  /**
   * Set the type names to use instead of the file name prefix.
   * 
   * @param typeNameAliases The type names to use instead of the file name
   *          prefix.
   */
  public void setTypeNameAliases(
    final Map<String, String> typeNameAliases) {
    this.typeNameAliases = typeNameAliases;
  }
}
