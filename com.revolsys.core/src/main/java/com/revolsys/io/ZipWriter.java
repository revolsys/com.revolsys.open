package com.revolsys.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The ZipWriter is a wrapper for another writer which has been configured to
 * write files to the tempDirectory. When this writer closes it will output a
 * zip file of the tempDirectory contents to the output stream and then delete
 * the temporary directory.
 * 
 * @author Paul Austin
 * @param <T> The type of data to write.
 */
public class ZipWriter<T> extends DelegatingWriter<T> {

  private File tempDirectory;

  private OutputStream out;

  public ZipWriter(
    File tempDirectory,
    Writer writer,
    OutputStream out) {
    super(writer);
    this.tempDirectory = tempDirectory;
    this.out = out;
  }

  @Override
  public void close() {
    try {
      super.close();
    } finally {
      try {
        ZipUtil.zipDirectory(tempDirectory, out);
      } catch (IOException e) {
        throw new RuntimeException("Unable to compress files", e);
      } finally {
        FileUtil.deleteDirectory(tempDirectory);
      }
    }
  }
}
