package com.revolsys.ui.html.fields;

import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.HtmlUtil;

public class AutoCompleteTextField extends TextField {
  private int maxResults = 25;

  private String dataUrl;

  public AutoCompleteTextField() {
  }

  public AutoCompleteTextField(final String name, final String dataUrl,
    final boolean required) {
    super(name, required);
    this.dataUrl = dataUrl;
  }

  public String getDataUrl() {
    return dataUrl;
  }

  public int getMaxResults() {
    return maxResults;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    super.serializeElement(out);
    out.startTag(HtmlUtil.SCRIPT);
    out.attribute(HtmlUtil.ATTR_TYPE, "text/javascript");
    out.text("$(document).ready(function() {\n");
    out.text("  $('#");
    out.text(getForm().getName());
    out.text(" input[name=\"");
    out.text(getName());
    out.text("\"]').autocomplete({\n");
    out.text("    minLength: 3,\n");
    out.text("    source: function(request, response) {\n");
    out.text("      $.ajax({\n");
    out.text("        url: '");
    out.text(dataUrl);
    out.text("',");
    out.text("        dataType: 'json',");
    out.text("        data: {");
    out.text("          maxRows: ");
    out.text(maxResults);
    out.text(",\n");
    out.text("          term: request.term\n");
    out.text("        },\n");
    out.text("        success: response\n");
    out.text("      });\n");
    out.text("    }\n");
    out.text("  });\n");
    out.text("});\n");
    out.endTag(HtmlUtil.SCRIPT);
  }

  public void setDataUrl(final String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }
}
