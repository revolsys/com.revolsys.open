package com.revolsys.record.io.format.xml.stax;

import com.revolsys.record.io.format.xml.XmlElementName;
import com.revolsys.record.io.format.xml.XmlElementNameProxy;

public class StaxProperty implements XmlElementNameProxy {

  private final XmlElementName elementName;

  private final String name;

  private final StaxElementFunction<?> reader;

  private final boolean list;

  public StaxProperty(final XmlElementName elementName, final String name,
    final StaxElementFunction<?> reader, final boolean list) {
    this.elementName = elementName;
    this.name = name;
    this.reader = reader;
    this.list = list;
  }

  @Override
  public XmlElementName getElementName() {
    return this.elementName;
  }

  public String getName() {
    return this.name;
  }

  public void handleElement(final StaxReader in, final StaxElementCallback callback) {
    callback.handleElement(in, this, callback);
  }

  public Object handleElementValue(final StaxReader in, final StaxElementCallback callback) {
    return this.reader.handle(in, callback);
  }

  public boolean isList() {
    return this.list;
  }

}
