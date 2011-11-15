package com.revolsys.ui.html.fields;


import javax.servlet.http.HttpServletRequest;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.ui.html.HtmlUtil;

public class AutoCompleteTextField extends TextField {
  private int maxResults = 25;

  private String dataUrl;

  private HttpServletRequest request;

  public AutoCompleteTextField() {
  }

  public void serializeElement(final XmlWriter out) {
    super.serializeElement(out);
    String url = dataUrl;

    if (url.startsWith("/")) {
      url = request.getContextPath() + url;
    }

    out.startTag(HtmlUtil.SCRIPT);
    out.attribute(HtmlUtil.ATTR_TYPE, "text/javascript");
    out.text("$(document).ready(function() {\n");
    out.text("  $('#" + getName() + "').autocomplete('" + url + "', {max: "
      + maxResults + "});\n");
    out.text("});\n");
    out.endTag(HtmlUtil.SCRIPT);
  }

  public void initialize(final HttpServletRequest request) {
    this.request = request;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }

  public String getDataUrl() {
    return dataUrl;
  }

  public void setDataUrl(final String dataUrl) {
    this.dataUrl = dataUrl;
  }
}
