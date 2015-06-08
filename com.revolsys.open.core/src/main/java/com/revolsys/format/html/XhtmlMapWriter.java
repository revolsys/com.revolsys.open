package com.revolsys.format.html;

import java.io.Writer;
import java.net.URI;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.format.xml.XmlWriter;
import com.revolsys.io.AbstractMapWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.NamedObject;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;

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
  @Override
  public void close() {
    if (this.out != null) {
      try {
        if (this.opened) {
          this.out.endTag(HtmlUtil.TABLE);
          this.out.endTag(HtmlUtil.DIV);
          this.out.endTag(HtmlUtil.DIV);
          if (this.wrap) {
            this.out.endTag(HtmlUtil.BODY);
            this.out.endTag(HtmlUtil.HTML);
          }
        }
        this.out.flush();
      } finally {
        if (this.wrap) {
          FileUtil.closeSilent(this.out);
        }
        this.out = null;
      }
    }
  }

  @Override
  public void flush() {
    this.out.flush();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    if (name.equals(IoConstants.WRAP_PROPERTY)) {
      this.wrap = Boolean.valueOf(value.toString());
    } else if (name.equals(IoConstants.TITLE_PROPERTY)) {
      this.title = value.toString();
    }
    super.setProperty(name, value);
  }

  @Override
  public void write(final Map<String, ? extends Object> values) {
    if (!this.opened) {
      if (this.title == null) {
        if (values instanceof NamedObject) {
          final String name = ((NamedObject)values).getName();
          if (name != null) {
            this.title = name;
          }
        }
      }
      if (this.wrap) {
        writeHeader();
      }
      this.out.startTag(HtmlUtil.DIV);
      if (this.title != null) {
        this.out.element(HtmlUtil.H1, this.title);
      }
      this.out.startTag(HtmlUtil.DIV);
      this.out.attribute(HtmlUtil.ATTR_CLASS, "objectView");
      this.out.startTag(HtmlUtil.TABLE);
      this.out.attribute(HtmlUtil.ATTR_CLASS, "data");
      this.opened = true;
    }
    this.out.startTag(HtmlUtil.TBODY);

    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final Object key = field.getKey();
      final Object value = field.getValue();
      if (isWritable(value)) {
        this.out.startTag(HtmlUtil.TR);
        // TODO case converter on key name
        this.out.element(HtmlUtil.TH, CaseConverter.toCapitalizedWords(key.toString()));
        this.out.startTag(HtmlUtil.TD);
        if (value instanceof URI) {
          HtmlUtil.serializeA(this.out, null, value, value);
        } else {
          this.out.text(value);
        }
        this.out.endTag(HtmlUtil.TD);
        this.out.endTag(HtmlUtil.TR);
      }
    }
    this.out.endTag(HtmlUtil.TBODY);
  }

  private void writeHeader() {
    this.out.startTag(HtmlUtil.HTML);

    this.out.startTag(HtmlUtil.HEAD);
    this.out.element(HtmlUtil.TITLE, this.title);

    this.out.endTag(HtmlUtil.HEAD);

    this.out.startTag(HtmlUtil.BODY);
  }
}
