package com.revolsys.io.csv;

import java.io.OutputStreamWriter;
import java.io.Writer;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.spring.SpringUtil;

import java.io.OutputStream;

import org.springframework.core.io.Resource;

public class CsvMapIoFactory extends AbstractIoFactory implements
  MapWriterFactory {
  public CsvMapIoFactory() {
    super("Comma Seprated Variable");
    addMediaTypeAndFileExtension(CsvConstants.MEDIA_TYPE,
      CsvConstants.FILE_EXTENSION);
  }

  public MapWriter getWriter(Resource resource) {
    Writer writer = SpringUtil.getWriter(resource);
    return getWriter(writer);
  }

  public MapWriter getWriter(OutputStream out) {
    Writer writer = new OutputStreamWriter(out);
    return getWriter(writer);
  }

  public MapWriter getWriter(final Writer out) {
    return new CsvMapWriter(out);
  }

  public boolean isCustomAttributionSupported() {
    return true;
  }

  public boolean isGeometrySupported() {
    return true;
  }
}
