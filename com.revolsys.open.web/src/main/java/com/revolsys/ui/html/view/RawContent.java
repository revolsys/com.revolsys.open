package com.revolsys.ui.html.view;

import org.springframework.core.io.Resource;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.spring.resource.SpringUtil;

/**
 * @author paustin
 * @version 1.0
 */
public class RawContent extends Element {
  private final String content;

  public RawContent(final Resource resource) {
    this(SpringUtil.getContents(resource));
  }

  public RawContent(final String content) {
    this.content = content;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    out.write(this.content);
  }
}
