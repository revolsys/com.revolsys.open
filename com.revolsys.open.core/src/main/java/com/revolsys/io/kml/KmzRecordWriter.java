package com.revolsys.io.kml;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.revolsys.data.record.Record;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;

public class KmzRecordWriter extends AbstractWriter<Record> {

  private final KmlRecordWriter kmlWriter;

  private final ZipOutputStream zipOut;

  public KmzRecordWriter(final OutputStream out, final Charset charset) {
    try {
      final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
        out);
      zipOut = new ZipOutputStream(bufferedOutputStream);
      final ZipEntry entry = new ZipEntry("doc.kml");
      zipOut.putNextEntry(entry);
      final OutputStreamWriter writer = FileUtil.createUtf8Writer(zipOut);
      kmlWriter = new KmlRecordWriter(writer);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to create KMZ file", e);
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
  public void open() {
    kmlWriter.open();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    kmlWriter.setProperty(name, value);
  }

  @Override
  public String toString() {
    return "KMZ Writer";
  }

  @Override
  public void write(final Record object) {
    kmlWriter.write(object);
  }

}
