package com.revolsys.record.io.format.xml.stax;

import com.revolsys.record.io.format.xml.XmlName;

public interface XmlElementCallback {
  public void attribute(XmlName name, String value);

  public void end(XmlName name);

  public void start(XmlName name);

}
