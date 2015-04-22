package com.revolsys.format.html;

import com.revolsys.format.xml.XmlWriter;

public class Aria {
  public static void controls(final XmlWriter out, final String value) {
    out.attribute("aria-controls", value);
  }

  public static void expanded(final XmlWriter out, final boolean expanded) {
    out.attribute("aria-expanded", expanded);
  }

  public static void labelledby(final XmlWriter out, final String value) {
    out.attribute("aria-labelledby", value);
  }
}
