package com.revolsys.xhtml;

import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.NamedObject;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.xml.io.XmlWriter;

public class XhtmlMapWriter extends AbstractMapWriter {

  private boolean opened = false;

  /** The writer */
  private XmlWriter out;

  private String title = "Items";

  private boolean wrap = true;

  public XhtmlMapWriter(final Writer out) {
    this.out = new XmlWriter(out);

  }

  /**
   * Closes the underlying writer.
   */
  public void close() {
    if (out != null) {
      try {
        if (opened) {
          out.endTag(HtmlUtil.TABLE);
          out.endTag(HtmlUtil.DIV);
          out.endTag(HtmlUtil.DIV);
          if (wrap) {
            out.endTag(HtmlUtil.BODY);
            out.endTag(HtmlUtil.HTML);
          }
        }
        out.flush();
      } finally {
        if (wrap) {
          FileUtil.closeSilent(out);
        }
        out = null;
      }
    }
  }

  public void flush() {
    out.flush();
  }

  public void setProperty(final String name, final Object value) {
    if (name.equals(IoConstants.WRAP_PROPERTY)) {
      wrap = Boolean.valueOf(value.toString());
    } else if (name.equals(IoConstants.TITLE_PROPERTY)) {
      title = value.toString();
    }
    super.setProperty(name, value);
  }

  public void write(final Map<String, ? extends Object> values) {
    if (!opened) {
      if (title == null) {
        if (values instanceof NamedObject) {
          String name = ((NamedObject)values).getName();
          if (name != null) {
            this.title = name;
          }
        }
      }
      if (wrap) {
        writeHeader();
      }
      out.startTag(HtmlUtil.DIV);
      if (title != null) {
        out.element(HtmlUtil.H1, title);
      }
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "objectView");
      out.startTag(HtmlUtil.TABLE);
      out.attribute(HtmlUtil.ATTR_CLASS, "data");
      opened = true;
    }
    out.startTag(HtmlUtil.TBODY);

    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final Object key = field.getKey();
      final Object value = field.getValue();
      out.startTag(HtmlUtil.TR);
      // TODO case converter on key name
      out.element(HtmlUtil.TH, CaseConverter.toCapitalizedWords(key.toString()));
      out.startTag(HtmlUtil.TD);
      if (value instanceof URI) {
        HtmlUtil.serializeA(out, null, value, value);
      } else {
        out.text(value);
      }
      out.endTag(HtmlUtil.TD);
      out.endTag(HtmlUtil.TR);
    }
    out.endTag(HtmlUtil.TBODY);
  }

  private void writeHeader() {
    out.startTag(HtmlUtil.HTML);

    out.startTag(HtmlUtil.HEAD);
    out.element(HtmlUtil.TITLE, title);

    out.endTag(HtmlUtil.HEAD);

    out.startTag(HtmlUtil.BODY);
  }
}
