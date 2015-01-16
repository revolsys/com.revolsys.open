package com.revolsys.io.html;

import java.io.Writer;
import java.net.URI;
import java.util.List;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.AbstractRecordWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.util.Property;

public class XhtmlRecordWriter extends AbstractRecordWriter {

  private String cssClass;

  private final RecordDefinition recordDefinition;

  private boolean opened = false;

  /** The writer */
  private XmlWriter out;

  private boolean singleObject;

  private String title;

  private boolean wrap = true;

  public XhtmlRecordWriter(final RecordDefinition recordDefinition,
    final Writer out) {
    this.recordDefinition = recordDefinition;
    this.out = new XmlWriter(out);
  }

  /**
   * Closes the underlying writer.
   */
  @Override
  public void close() {
    if (this.out != null) {
      try {
        writeFooter();
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
    super.setProperty(name, value);
    if (value != null) {
      if (name.equals(IoConstants.WRAP_PROPERTY)) {
        this.wrap = Boolean.valueOf(value.toString());
      } else if (name.equals(IoConstants.TITLE_PROPERTY)) {
        this.title = value.toString();
      } else if (name.equals(IoConstants.CSS_CLASS)) {
        this.cssClass = value.toString();
      }
    }
  }

  @Override
  public void write(final Record object) {
    if (!this.opened) {
      writeHeader();
    }
    if (this.singleObject) {
      for (final String key : this.recordDefinition.getFieldNames()) {
        final Object value = object.getValue(key);
        if (isWritable(value)) {
          this.out.startTag(HtmlUtil.TR);
          this.out.element(HtmlUtil.TH,
            CaseConverter.toCapitalizedWords(key.toString()));
          this.out.startTag(HtmlUtil.TD);
          if (value == null) {
            this.out.text("-");
          } else if (value instanceof URI) {
            HtmlUtil.serializeA(this.out, null, value, value);
          } else {
            writeValue(key, value);
          }
          this.out.endTag(HtmlUtil.TD);
          this.out.endTag(HtmlUtil.TR);
        }
      }
    } else {
      this.out.startTag(HtmlUtil.TR);
      for (final String key : this.recordDefinition.getFieldNames()) {
        final Object value = object.getValue(key);
        this.out.startTag(HtmlUtil.TD);
        if (value == null) {
          this.out.text("-");
        }
        if (value instanceof URI) {
          HtmlUtil.serializeA(this.out, null, value, value);
        } else {
          writeValue(key, value);
        }
        this.out.endTag(HtmlUtil.TD);
      }
      this.out.endTag(HtmlUtil.TR);

    }
  }

  private void writeFooter() {
    if (this.opened) {
      this.out.endTag(HtmlUtil.TBODY);
      this.out.endTag(HtmlUtil.TABLE);
      this.out.endTag(HtmlUtil.DIV);
      this.out.endTag(HtmlUtil.DIV);
      if (this.wrap) {
        this.out.endTag(HtmlUtil.BODY);
        this.out.endTag(HtmlUtil.HTML);
      }
    }
  }

  @SuppressWarnings("rawtypes")
  private void writeHeader() {
    setIndent(isIndent());
    if (this.wrap) {
      this.out.startDocument("UTF-8", "1.0");
      this.out.startTag(HtmlUtil.HTML);

      this.out.startTag(HtmlUtil.HEAD);

      this.out.startTag(HtmlUtil.META);
      this.out.attribute(HtmlUtil.ATTR_HTTP_EQUIV, "Content-Type");
      this.out.attribute(HtmlUtil.ATTR_CONTENT, "text/html; charset=utf-8");
      this.out.endTag(HtmlUtil.META);

      if (Property.hasValue(this.title)) {
        this.out.element(HtmlUtil.TITLE, this.title);
      }

      final Object style = getProperty("htmlCssStyleUrl");
      if (style instanceof String) {
        final String styleUrl = (String)style;
        this.out.startTag(HtmlUtil.LINK);
        this.out.attribute(HtmlUtil.ATTR_HREF, styleUrl);
        this.out.attribute(HtmlUtil.ATTR_REL, "stylesheet");
        this.out.attribute(HtmlUtil.ATTR_TYPE, "text/css");
        this.out.endTag(HtmlUtil.LINK);
      } else if (style instanceof List) {
        final List styleUrls = (List)style;
        for (final Object styleUrl : styleUrls) {
          this.out.startTag(HtmlUtil.LINK);
          this.out.attribute(HtmlUtil.ATTR_HREF, styleUrl);
          this.out.attribute(HtmlUtil.ATTR_REL, "stylesheet");
          this.out.attribute(HtmlUtil.ATTR_TYPE, "text/css");
          this.out.endTag(HtmlUtil.LINK);
        }
      }

      this.out.endTag(HtmlUtil.HEAD);

      this.out.startTag(HtmlUtil.BODY);
    }
    this.out.startTag(HtmlUtil.DIV);
    this.out.attribute(HtmlUtil.ATTR_CLASS, this.cssClass);
    if (this.title != null) {
      this.out.element(HtmlUtil.H1, this.title);
    }
    this.singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (this.singleObject) {
      this.out.startTag(HtmlUtil.DIV);
      this.out.attribute(HtmlUtil.ATTR_CLASS, "objectView");
      this.out.startTag(HtmlUtil.TABLE);
      this.out.attribute(HtmlUtil.ATTR_CLASS, "data");
      this.out.startTag(HtmlUtil.TBODY);
    } else {
      this.out.startTag(HtmlUtil.DIV);
      this.out.attribute(HtmlUtil.ATTR_CLASS, "objectList");
      this.out.startTag(HtmlUtil.TABLE);
      this.out.attribute(HtmlUtil.ATTR_CLASS, "data");

      this.out.startTag(HtmlUtil.THEAD);
      this.out.startTag(HtmlUtil.TR);
      for (final String name : this.recordDefinition.getFieldNames()) {
        this.out.element(HtmlUtil.TH, name);
      }
      this.out.endTag(HtmlUtil.TR);
      this.out.endTag(HtmlUtil.THEAD);

      this.out.startTag(HtmlUtil.TBODY);
    }
    this.opened = true;
  }

  public void writeValue(final String name, final Object value) {
    final DataType dataType = this.recordDefinition.getFieldType(name);

    @SuppressWarnings("unchecked")
    final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
    final StringConverter<Object> converter = StringConverterRegistry.getInstance()
        .getConverter(dataTypeClass);
    if (converter == null) {
      this.out.text(value);
    } else {
      final String stringValue = converter.toString(value);
      this.out.text(stringValue);
    }
  }
}
