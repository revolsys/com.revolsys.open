package com.revolsys.record.io.format.xml.stax;

import com.revolsys.record.io.format.xml.XmlName;
import com.revolsys.record.io.format.xml.XmlNameProxy;

public class StaxProperty implements XmlNameProxy {

  private final XmlName elementName;

  private final String name;

  private final StaxElementToObject<?> reader;

  private final boolean list;

  public StaxProperty(final XmlName elementName, final String name,
    final StaxElementToObject<?> reader, final boolean list) {
    this.elementName = elementName;
    this.name = name;
    this.reader = reader;
    this.list = list;
  }

  @Override
  public XmlName getXmlName() {
    return this.elementName;
  }

  public String getName() {
    return this.name;
  }

  public void handleElement(final StaxReader in, final StaxElementCallback callback) {
    callback.handleElement(in, this, callback);
  }

  public Object handleElementValue(final StaxReader in, final StaxElementCallback callback) {
    return this.reader.toObject(in);
  }

  public boolean isList() {
    return this.list;
  }

}
