package com.revolsys.io.map;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.revolsys.spring.resource.Resource;

import com.revolsys.io.FileIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.spring.resource.SpringUtil;

public interface MapWriterFactory extends FileIoFactory {
  default MapWriter createMapWriter(final Object source) {
    final Resource resource = com.revolsys.spring.resource.Resource.getResource(source);
    final Writer writer = resource.newWriter();
    return createMapWriter(writer);
  }

  default MapWriter createMapWriter(final OutputStream out) {
    final Writer writer = FileUtil.createUtf8Writer(out);
    return createMapWriter(writer);
  }

  default MapWriter createMapWriter(final OutputStream out, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(out, charset);
    return createMapWriter(writer);
  }

  MapWriter createMapWriter(final Writer out);
}
