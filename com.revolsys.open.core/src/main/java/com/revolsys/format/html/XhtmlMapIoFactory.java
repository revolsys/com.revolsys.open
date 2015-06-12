package com.revolsys.format.html;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import org.springframework.core.io.Resource;

import com.revolsys.io.AbstractIoFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.MapWriter;
import com.revolsys.io.MapWriterFactory;
import com.revolsys.spring.SpringUtil;

public class XhtmlMapIoFactory extends AbstractIoFactory implements MapWriterFactory {
  public XhtmlMapIoFactory() {
    super("XHMTL");
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out) {
    final Writer writer = FileUtil.createUtf8Writer(out);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final OutputStream out, final Charset charset) {
    final OutputStreamWriter writer = new OutputStreamWriter(out, charset);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final Resource resource) {
    final Writer writer = SpringUtil.getWriter(resource);
    return getMapWriter(writer);
  }

  @Override
  public MapWriter getMapWriter(final Writer out) {
    return new XhtmlMapWriter(out);
  }
}
