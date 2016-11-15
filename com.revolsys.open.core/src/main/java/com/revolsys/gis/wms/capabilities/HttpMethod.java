package com.revolsys.gis.wms.capabilities;

import java.net.URL;

import org.w3c.dom.Element;

import com.revolsys.record.io.format.xml.XmlUtil;
import com.revolsys.util.UrlUtil;

public class HttpMethod {
  private final String name;

  private final URL onlineResource;

  public HttpMethod(final Element httpMethodElement) {
    this.name = httpMethodElement.getTagName();
    final String onlineResourceText = XmlUtil.getFirstElementAttribute(httpMethodElement,
      "OnlineResource", "http://www.w3.org/1999/xlink", "href");
    this.onlineResource = UrlUtil.getUrl(onlineResourceText);
  }

  public String getName() {
    return this.name;
  }

  public URL getOnlineResource() {
    return this.onlineResource;
  }

  @Override
  public String toString() {
    return this.name + " " + this.onlineResource;
  }
}
