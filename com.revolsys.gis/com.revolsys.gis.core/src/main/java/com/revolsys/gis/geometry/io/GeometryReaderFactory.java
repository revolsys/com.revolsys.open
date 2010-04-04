package com.revolsys.gis.geometry.io;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;

import com.revolsys.io.IoFactory;
import com.vividsolutions.jts.geom.Geometry;

public interface GeometryReaderFactory extends IoFactory {
  com.revolsys.gis.data.io.Reader<Geometry> createGeometryReader(
    final InputStream in);

  com.revolsys.gis.data.io.Reader<Geometry> createGeometryReader(
    final InputStream in,
    Charset charset);

  com.revolsys.gis.data.io.Reader<Geometry> createGeometryReader(
    final Reader in);

}
