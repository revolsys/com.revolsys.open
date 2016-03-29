package com.revolsys.ui.html.fields;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.util.HtmlAttr;
import com.revolsys.util.HtmlElem;

public class AutoCompleteTextField extends TextField {
  private String dataUrl;

  private int maxResults = 25;

  public AutoCompleteTextField() {
  }

  public AutoCompleteTextField(final String name, final String dataUrl, final boolean required) {
    super(name, required);
    this.dataUrl = dataUrl;
  }

  public String getDataUrl() {
    return this.dataUrl;
  }

  public int getMaxResults() {
    return this.maxResults;
  }

  @Override
  public void serializeElement(final XmlWriter out) {
    super.serializeElement(out);
    out.startTag(HtmlElem.SCRIPT);
    out.attribute(HtmlAttr.TYPE, "text/javascript");
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
    out.text(this.dataUrl);
    out.text("',");
    out.text("        dataType: 'json',");
    out.text("        data: {");
    out.text("          maxRows: ");
    out.text(this.maxResults);
    out.text(",\n");
    out.text("          term: request.term\n");
    out.text("        },\n");
    out.text("        success: response\n");
    out.text("      });\n");
    out.text("    }\n");
    out.text("  });\n");
    out.text("});\n");
    out.endTag(HtmlElem.SCRIPT);
  }

  public void setDataUrl(final String dataUrl) {
    this.dataUrl = dataUrl;
  }

  public void setMaxResults(final int maxResults) {
    this.maxResults = maxResults;
  }
}
