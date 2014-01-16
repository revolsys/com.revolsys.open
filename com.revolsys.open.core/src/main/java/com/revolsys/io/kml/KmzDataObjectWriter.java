package com.revolsys.io.kml;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;

public class KmzDataObjectWriter extends AbstractWriter<DataObject> {

  private final KmlDataObjectWriter kmlWriter;

  private final ZipOutputStream zipOut;

  public KmzDataObjectWriter(final OutputStream out, final Charset charset) {
    try {
      final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(
        out);
      zipOut = new ZipOutputStream(bufferedOutputStream);
      final ZipEntry entry = new ZipEntry("doc.kml");
      zipOut.putNextEntry(entry);
      final OutputStreamWriter writer = FileUtil.createUtf8Writer(zipOut);
      kmlWriter = new KmlDataObjectWriter(writer);
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
  public void write(final DataObject object) {
    kmlWriter.write(object);
  }

}
