package com.revolsys.gis.html;

import java.io.Writer;
import java.net.URI;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.xml.io.XmlWriter;

public class XhtmlDataObjectWriter extends AbstractWriter<DataObject> {

  private boolean opened = false;

  /** The writer */
  private XmlWriter out;

  private String title;

  private boolean wrap = true;

  private String cssClass;

  private DataObjectMetaData metaData;

  public XhtmlDataObjectWriter(
    DataObjectMetaData metaData,
    final Writer out) {
    this.metaData = metaData;
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

  @Override
  public void setProperty(
    final String name,
    final Object value) {
    super.setProperty(name, value);
    if (value != null) {
      if (name.equals("wrap")) {
        wrap = Boolean.valueOf(value.toString());
      } else if (name.equals("title")) {
        title = value.toString();
      } else if (name.equals("cssClass")) {
        cssClass = value.toString();
      }
    }
  }

  public void write(
    final DataObject object) {
    if (!opened) {
      if (wrap) {
        writeHeader();
      }
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
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

    for (final String key : object.getMetaData().getAttributeNames()) {
      final Object value = object.getValue(key);
      out.startTag(HtmlUtil.TR);
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
    out.startDocument();
    out.startTag(HtmlUtil.HTML);

    out.startTag(HtmlUtil.HEAD);
    out.element(HtmlUtil.TITLE, title);

    out.endTag(HtmlUtil.HEAD);

    out.startTag(HtmlUtil.BODY);
  }
}
