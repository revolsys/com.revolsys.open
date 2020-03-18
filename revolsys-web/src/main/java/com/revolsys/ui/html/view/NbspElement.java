package com.revolsys.ui.html.view;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.decorator.Decorator;

public class NbspElement extends Element {
  public static final NbspElement INSTANCE = new NbspElement();

  @Override
  public void serializeElement(final XmlWriter out) {
    out.entityRef("nbsp");
  }

  @Override
  public void setContainer(final ElementContainer container) {
  }

  @Override
  public void setDecorator(final Decorator decorator) {
  }
}
