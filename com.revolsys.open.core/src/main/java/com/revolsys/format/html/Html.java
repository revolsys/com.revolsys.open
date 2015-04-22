package com.revolsys.format.html;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;

public class Html {
  public static void href(final XmlWriter out, final String url) {
    out.attribute(HtmlUtil.ATTR_HREF, url);
  }

}
