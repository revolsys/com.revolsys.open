package com.revolsys.record.io.format.html;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.revolsys.io.AbstractIoFactoryWithCoordinateSystem;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapWriter;
import com.revolsys.io.map.MapWriterFactory;
import com.revolsys.record.io.RecordWriter;
import com.revolsys.record.io.RecordWriterFactory;
import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.util.HtmlUtil;

public class Html extends AbstractIoFactoryWithCoordinateSystem
  implements MapWriterFactory, RecordWriterFactory {
  public static void href(final XmlWriter out, final String url) {
    out.attribute(HtmlUtil.ATTR_HREF, url);
  }

  public Html() {
    super("XHMTL");
    addMediaTypeAndFileExtension("text/html", "html");
    addMediaTypeAndFileExtension("application/xhtml+xml", "xhtml");
    addMediaTypeAndFileExtension("application/xhtml+xml", "html");
  }

  @Override
  public MapWriter newMapWriter(final Writer out) {
    return new XhtmlMapWriter(out);
  }

  @Override
  public RecordWriter newRecordWriter(final String baseName,
    final RecordDefinition recordDefinition, final OutputStream outputStream,
    final Charset charset) {
    final OutputStreamWriter writer = FileUtil.newUtf8Writer(outputStream);
    return new XhtmlRecordWriter(recordDefinition, writer);
  }
}
