package com.revolsys.format.kml;

import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.revolsys.spring.resource.Resource;

import com.revolsys.collection.iterator.AbstractIterator;
import com.revolsys.io.FileUtil;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.spring.resource.SpringUtil;

public class KmzGeometryIterator extends AbstractIterator<Geometry> {

  private KmlGeometryIterator kmlIterator;

  private ZipInputStream zipIn;

  public KmzGeometryIterator(final Resource resource) {
    try {
      final InputStream in = resource.getInputStream();
      this.zipIn = new ZipInputStream(in);
    } catch (final Throwable e) {
      throw new RuntimeException("Unable to reade KMZ file", e);
    }
  }

  @Override
  protected void doClose() {
    FileUtil.closeSilent(this.kmlIterator, this.zipIn);
    this.kmlIterator = null;
    this.zipIn = null;
  }

  @Override
  protected void doInit() {
    try {
      for (ZipEntry entry = this.zipIn.getNextEntry(); entry != null; entry = this.zipIn
        .getNextEntry()) {
        final String name = entry.getName();
        final String extension = FileUtil.getFileNameExtension(name);
        if ("kml".equals(extension)) {
          this.kmlIterator = new KmlGeometryIterator(this.zipIn);
          return;
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException("Unable to read KML file inside KMZ file", e);
    }
  }

  @Override
  protected Geometry getNext() {
    if (this.kmlIterator == null) {
      throw new NoSuchElementException();
    } else {
      return this.kmlIterator.getNext();
    }
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }
}
