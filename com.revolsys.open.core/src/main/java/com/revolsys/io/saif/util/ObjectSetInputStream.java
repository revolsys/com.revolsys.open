/*
 * $URL$
 * $Author$
 * $Date$
 * $Revision$

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
package com.revolsys.io.saif.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.log4j.Logger;

public class ObjectSetInputStream extends InputStream {
  private static final Logger log = Logger.getLogger(ObjectSetInputStream.class);

  private File directory;

  private final String fileName;

  private InputStream in;

  private int index;

  private final String prefix;

  private ZipFile zipFile;

  public ObjectSetInputStream(final File directory, final String fileName)
    throws IOException {
    this.directory = directory;
    this.fileName = fileName;
    prefix = ObjectSetUtil.getObjectSubsetPrefix(fileName);
    in = openFile(fileName);
    if (in == null) {
      throw new IllegalArgumentException("File " + fileName
        + " does not exist ");
    }
  }

  public ObjectSetInputStream(final ZipFile zipFile, final String fileName)
    throws IOException {
    this.zipFile = zipFile;
    this.fileName = fileName;
    in = openFile(fileName);
    prefix = ObjectSetUtil.getObjectSubsetPrefix(fileName);
    if (in == null) {
      throw new IllegalArgumentException("File " + fileName
        + " does not exist ");
    }
  }

  @Override
  public void close() throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("Closing object subset '" + fileName + "' from reading");
    }
    if (in != null) {
      in.close();
    }
  }

  private InputStream openFile(final String fileName) throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("Opening object subset '" + fileName + "' for reading");
    }
    if (zipFile != null) {
      final ZipEntry entry = zipFile.getEntry(fileName);
      if (entry != null) {
        return new BufferedInputStream(zipFile.getInputStream(entry));
      }
    } else {
      final File file = new File(directory, fileName);
      if (file.exists()) {
        return new BufferedInputStream(new FileInputStream(file));
      }
    }
    return null;
  }

  private boolean openNextFile() throws IOException {
    close();
    index++;
    final String fileName = ObjectSetUtil.getObjectSubsetName(prefix, index);

    in = openFile(fileName);
    if (in != null) {
      return true;
    } else {
      return false;
    }
  }

  @Override
  public int read() throws IOException {
    int bytesRead = in.read();
    if (bytesRead == -1) {
      if (!openNextFile()) {
        return -1;
      } else {
        bytesRead = in.read();
      }
    }
    return bytesRead;
  }

  @Override
  public int read(final byte[] b) throws IOException {
    return read(b, 0, b.length);
  }

  @Override
  public int read(final byte[] b, final int off, final int len)
    throws IOException {
    if (in == null) {
      return -1;
    } else {
      int bytesRead = in.read(b, off, len);
      if (bytesRead == -1) {
        if (!openNextFile()) {
          return -1;
        } else {
          bytesRead = in.read(b, off, len);
        }
      }
      return bytesRead;
    }
  }
}
