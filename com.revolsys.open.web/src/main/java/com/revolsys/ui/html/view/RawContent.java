package com.revolsys.ui.html.view;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.spring.resource.Resource;

/**
 * @author paustin
 * @version 1.0
 */
public class RawContent extends Element {
  private final String content;

  public RawContent(final Resource resource) {
    this(resource.contentsAsString());
  }

  public RawContent(final String content) {
    this.content = content;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.write(this.content);
  }
}
