/*
 * $URL:https://secure.revolsys.com/svn/open.revolsys.com/GIS/trunk/src/main/java/com/revolsys/gis/format/core/io/AbstractDirectoryReader.java $
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
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import com.revolsys.io.FileUtil;
import com.revolsys.io.filter.ExtensionFilenameFilter;

public abstract class AbstractDirectoryReader<T> extends AbstractReader<T>
  implements Iterator<T> {
  /** The list of base file names to read. */
  private List<String> baseFileNames = new ArrayList<String>();

  /** The current directory being processed. */
  private File currentFile;

  /** The current iterator. */
  private Iterator<T> currentIterator;

  /** The reader for the current directory being processed. */
  private Reader<T> currentReader;

  /** The directory of data to read from. */
  private File directory;

  /** The files to be read by this reader. */
  private Iterator<Entry<File, Reader<T>>> readerIterator;

  /** The filter used to select files from the directory. */
  private FilenameFilter fileNameFilter;

  /** The files to be read by this reader. */
  private List<File> files;

  /** Flag indicating if the reader has more objects to be read. */
  private boolean hasNext = true;

  /** The logging instance. */
  private final Logger log = Logger.getLogger(getClass());

  private Map<File, Reader<T>> readers = new LinkedHashMap<File, Reader<T>>();

  /**
   * Construct a new AbstractDirectoryReader.
   */
  public AbstractDirectoryReader() {
  }

  /**
   * Close the reader.
   */
  public void close() {
    if (currentReader != null) {
      currentReader.close();
    }
  }

  /**
   * Create a new {@link Reader} to read the file.
   * 
   * @param file The file to read.
   * @return The reader for the file.
   */
  protected abstract Reader<T> createReader(Resource file);

  /**
   * Get the list of base file names to read.
   * 
   * @return The list of base file names to read.
   */
  public List<String> getBaseFileNames() {
    return baseFileNames;
  }

  /**
   * Get the directory containing the files to read.
   * 
   * @return The directory containing the files to read.
   */
  public File getDirectory() {
    return directory;
  }

  /**
   * Get the filter used to select files from the directory.
   * 
   * @return The filter used to select files from the directory.
   */
  public FilenameFilter getFileNameFilter() {
    return fileNameFilter;
  }

  /**
   * Get the files that are to be read by this reader. This must be overwritten
   * in sub classes to return the files in the working directory that are to be
   * read by instances of the write returned by {@link #createReader(File)}.
   * 
   * @return The list of files.
   */
  protected List<File> getFiles() {
    final File[] files;
    if (fileNameFilter == null) {
      files = directory.listFiles();
    } else {
      files = directory.listFiles(fileNameFilter);
    }
    List<File> fileList;
    if (baseFileNames.isEmpty()) {
      Arrays.sort(files);
      fileList = Arrays.asList(files);
    } else {
      fileList = new ArrayList<File>();
      Map<String, File> fileBaseNameMap = new HashMap<String, File>();
      for (File file : files) {
        String baseName = FileUtil.getBaseName(file);
        fileBaseNameMap.put(baseName, file);
      }
      for (String baseName : baseFileNames) {
        File file = fileBaseNameMap.get(baseName);
        if (file != null) {
          fileList.add(file);
        }
      }
    }
    return fileList;
  }

  @PostConstruct
  public void open() {
    for (File file : getFiles()) {
      final FileSystemResource resource = new FileSystemResource(file);
      Reader<T> reader = createReader(resource);
      reader.open();
      if (reader != null) {
        readers.put(file, reader);
      }
    }
    readerIterator = readers.entrySet().iterator();
    hasNext();
  }

  /**
   * Check to see if the reader has more data objects to be read.
   * 
   * @return True if the reader has more data objects to be read.
   */
  public boolean hasNext() {
    while (hasNext && (currentIterator == null || !currentIterator.hasNext())) {
      if (readerIterator.hasNext()) {
        if (currentReader != null) {
          try {
            currentReader.close();
          } catch (final Throwable t) {
            log.warn(t.getMessage(), t);
          }
        }
        Entry<File, Reader<T>> entry = readerIterator.next();
        currentFile = entry.getKey();
        try {
          currentReader = entry.getValue();
          currentIterator = currentReader.iterator();
          hasNext = currentIterator.hasNext();
        } catch (final Throwable e) {
          hasNext = false;
          log.error(e.getMessage(), e);
        }
      } else {
        hasNext = false;
      }
    }
    return hasNext;
  }

  /**
   * Get the iterator.
   * 
   * @return The iterator.
   */
  public Iterator<T> iterator() {
    return this;
  }

  /**
   * Get the next data object read by this reader.
   * 
   * @return The next DataObject.
   * @exception NoSuchElementException If the reader has no more data objects.
   */
  public T next() {
    if (hasNext()) {
      return currentIterator.next();
    } else {
      throw new NoSuchElementException();
    }
  }

  /**
   * Removing data objects is not supported.
   */
  public void remove() {
    throw new UnsupportedOperationException();
  }

  /**
   * Set the list of base file names to read.
   * 
   * @param baseFileNames The list of base file names to read.
   */
  public void setBaseFileNames(final List<String> baseFileNames) {
    this.baseFileNames = baseFileNames;
  }

  /**
   * Set the directory containing the files to read.
   * 
   * @param directory The directory containing the files to read.
   */
  public void setDirectory(final File directory) {
    if (!directory.isDirectory()) {
      throw new IllegalArgumentException("File must exist and be a directory "
        + directory);
    } else {
      this.directory = directory;
      files = getFiles();

    }
  }

  public void setFileExtensions(final Collection<String> fileExtensions) {
    this.fileNameFilter = new ExtensionFilenameFilter(fileExtensions);
  }

  public void setFileExtensions(final String... fileExtensions) {
    setFileExtensions(Arrays.asList(fileExtensions));
  }

  /**
   * Set the filter used to select files from the directory.
   * 
   * @param fileNameFilter The filter used to select files from the directory.
   */
  public void setFileNameFilter(final FilenameFilter fileNameFilter) {
    this.fileNameFilter = fileNameFilter;
  }

}
