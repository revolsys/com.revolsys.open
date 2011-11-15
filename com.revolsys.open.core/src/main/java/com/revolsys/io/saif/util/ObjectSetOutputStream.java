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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.apache.log4j.Logger;

public class ObjectSetOutputStream extends OutputStream {
  private static final Logger log = Logger.getLogger(ObjectSetOutputStream.class);

  private File file;

  private short index = 0;

  private int maxSize = Integer.MAX_VALUE;

  private OutputStream out;

  private final String prefix;

  private int size = 0;

  public ObjectSetOutputStream(
    final File file)
    throws IOException {
    this.file = file;
    prefix = ObjectSetUtil.getObjectSubsetPrefix(file);
    openFile();
  }

  public ObjectSetOutputStream(
    final File file,
    final int maxSize)
    throws IOException {
    this(file);
    this.maxSize = maxSize;
  }

  @Override
  public void close()
    throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("Closing object subset '" + file.getName() + "' from writing");
    }
    out.close();
  }

  @Override
  public void flush()
    throws IOException {
    out.flush();
  }

  private void openFile()
    throws IOException {
    if (log.isDebugEnabled()) {
      log.debug("Creating object subset '" + file.getName() + "'");
    }
    out = new BufferedOutputStream(new FileOutputStream(file), 4096);
  }

  private void openNextFile()
    throws IOException {
    out.write('\n');
    out.flush();
    close();
    index++;
    final String fileName = ObjectSetUtil.getObjectSubsetName(prefix, index);
    file = new File(file.getParentFile(), fileName);
    size = 0;
    openFile();
  }

  @Override
  public void write(
    final byte[] b)
    throws IOException {
    write(b, 0, b.length);
  }

  @Override
  public void write(
    final byte[] b,
    final int off,
    final int len)
    throws IOException {
    if (size >= maxSize) {
      openNextFile();
      size = 0;
    }
    out.write(b, off, len);
    size += len;
  }

  @Override
  public void write(
    final int b)
    throws IOException {
    if (size >= maxSize) {
      openNextFile();
      size = 0;
    }
    out.write(b);
    size += 1;
  }
}
