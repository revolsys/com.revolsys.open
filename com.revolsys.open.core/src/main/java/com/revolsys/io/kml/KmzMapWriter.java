package com.revolsys.io.kml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;

public class KmzMapWriter extends AbstractMapWriter {

  private KmlMapWriter kmlWriter;

  private final ZipOutputStream zipOut;

  public KmzMapWriter(final OutputStream out) {
    try {
      zipOut = new ZipOutputStream(out);
      final ZipEntry entry = new ZipEntry("doc.kml");
      zipOut.putNextEntry(entry);
      final java.io.Writer writer = FileUtil.createUtf8Writer(zipOut);
      kmlWriter = new KmlMapWriter(writer);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create KMZ file ", e);
    }

  }

  @Override
  public void close() {
    try {
      kmlWriter.close();
      zipOut.close();
    } catch (final IOException e) {
    }
  }

  @Override
  public void flush() {
    try {
      kmlWriter.flush();
      zipOut.flush();
    } catch (final IOException e) {
      throw new RuntimeException("Unable to flush: ", e);
    }
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    kmlWriter.write(values);
  }

}
