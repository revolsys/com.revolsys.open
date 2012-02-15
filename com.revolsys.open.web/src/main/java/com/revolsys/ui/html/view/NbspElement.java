package com.revolsys.ui.html.view;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.decorator.Decorator;

public class NbspElement extends Element {
  public static final NbspElement INSTANCE = new NbspElement();

  @Override
  public void setContainer(ElementContainer container) {
  }

  @Override
  public void setDecorator(Decorator decorator) {
  }

  @Override
  public void serializeElement(XmlWriter out) {
    out.entityRef("nbsp");
  }
}
