package com.revolsys.io.html;

import java.io.Writer;
import java.net.URI;
import java.util.List;

import org.springframework.util.StringUtils;

import com.revolsys.converter.string.StringConverter;
import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.xml.XmlWriter;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;

public class XhtmlRecordWriter extends AbstractWriter<Record> {

  private String cssClass;

  private final RecordDefinition metaData;

  private boolean opened = false;

  /** The writer */
  private XmlWriter out;

  private boolean singleObject;

  private String title;

  private boolean wrap = true;

  public XhtmlRecordWriter(final RecordDefinition metaData,
    final Writer out) {
    this.metaData = metaData;
    this.out = new XmlWriter(out);
  }

  /**
   * Closes the underlying writer.
   */
  @Override
  public void close() {
    if (out != null) {
      try {
        writeFooter();
        out.flush();
      } finally {
        if (wrap) {
          FileUtil.closeSilent(out);
        }
        out = null;
      }
    }
  }

  @Override
  public void flush() {
    out.flush();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (value != null) {
      if (name.equals(IoConstants.WRAP_PROPERTY)) {
        wrap = Boolean.valueOf(value.toString());
      } else if (name.equals(IoConstants.TITLE_PROPERTY)) {
        title = value.toString();
      } else if (name.equals(IoConstants.CSS_CLASS_PROPERTY)) {
        cssClass = value.toString();
      }
    }
  }

  @Override
  public void write(final Record object) {
    if (!opened) {
      writeHeader();
    }
    if (singleObject) {
      for (final String key : metaData.getAttributeNames()) {
        final Object value = object.getValue(key);
        out.startTag(HtmlUtil.TR);
        out.element(HtmlUtil.TH,
          CaseConverter.toCapitalizedWords(key.toString()));
        out.startTag(HtmlUtil.TD);
        if (value == null) {
          out.text("-");
        } else if (value instanceof URI) {
          HtmlUtil.serializeA(out, null, value, value);
        } else {
          writeValue(key, value);
        }
        out.endTag(HtmlUtil.TD);
        out.endTag(HtmlUtil.TR);
      }
    } else {
      out.startTag(HtmlUtil.TR);
      for (final String key : metaData.getAttributeNames()) {
        final Object value = object.getValue(key);
        out.startTag(HtmlUtil.TD);
        if (value == null) {
          out.text("-");
        }
        if (value instanceof URI) {
          HtmlUtil.serializeA(out, null, value, value);
        } else {
          writeValue(key, value);
        }
        out.endTag(HtmlUtil.TD);
      }
      out.endTag(HtmlUtil.TR);

    }
  }

  private void writeFooter() {
    if (opened) {
      out.endTag(HtmlUtil.TBODY);
      out.endTag(HtmlUtil.TABLE);
      out.endTag(HtmlUtil.DIV);
      out.endTag(HtmlUtil.DIV);
      if (wrap) {
        out.endTag(HtmlUtil.BODY);
        out.endTag(HtmlUtil.HTML);
      }
    }
  }

  private void writeHeader() {
    if (wrap) {
      out.startDocument("UT-8", "1.0");
      out.startTag(HtmlUtil.HTML);

      out.startTag(HtmlUtil.HEAD);

      out.startTag(HtmlUtil.META);
      out.attribute(HtmlUtil.ATTR_HTTP_EQUIV, "Content-Type");
      out.attribute(HtmlUtil.ATTR_CONTENT, "text/html; charset=utf-8");
      out.endTag(HtmlUtil.META);

      if (StringUtils.hasText(title)) {
        out.element(HtmlUtil.TITLE, title);
      }

      final Object style = getProperty("htmlCssStyleUrl");
      if (style instanceof String) {
        final String styleUrl = (String)style;
        out.startTag(HtmlUtil.LINK);
        out.attribute(HtmlUtil.ATTR_HREF, styleUrl);
        out.attribute(HtmlUtil.ATTR_REL, "stylesheet");
        out.attribute(HtmlUtil.ATTR_TYPE, "text/css");
        out.endTag(HtmlUtil.LINK);
      } else if (style instanceof List) {
        final List styleUrls = (List)style;
        for (final Object styleUrl : styleUrls) {
          out.startTag(HtmlUtil.LINK);
          out.attribute(HtmlUtil.ATTR_HREF, styleUrl);
          out.attribute(HtmlUtil.ATTR_REL, "stylesheet");
          out.attribute(HtmlUtil.ATTR_TYPE, "text/css");
          out.endTag(HtmlUtil.LINK);
        }
      }

      out.endTag(HtmlUtil.HEAD);

      out.startTag(HtmlUtil.BODY);
    }
    out.startTag(HtmlUtil.DIV);
    out.attribute(HtmlUtil.ATTR_CLASS, cssClass);
    if (title != null) {
      out.element(HtmlUtil.H1, title);
    }
    singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (singleObject) {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "objectView");
      out.startTag(HtmlUtil.TABLE);
      out.attribute(HtmlUtil.ATTR_CLASS, "data");
      out.startTag(HtmlUtil.TBODY);
    } else {
      out.startTag(HtmlUtil.DIV);
      out.attribute(HtmlUtil.ATTR_CLASS, "objectList");
      out.startTag(HtmlUtil.TABLE);
      out.attribute(HtmlUtil.ATTR_CLASS, "data");

      out.startTag(HtmlUtil.THEAD);
      out.startTag(HtmlUtil.TR);
      for (final String name : metaData.getAttributeNames()) {
        out.element(HtmlUtil.TH, name);
      }
      out.endTag(HtmlUtil.TR);
      out.endTag(HtmlUtil.THEAD);

      out.startTag(HtmlUtil.TBODY);
    }
    opened = true;
  }

  public void writeValue(final String name, final Object value) {
    final DataType dataType = metaData.getAttributeType(name);

    @SuppressWarnings("unchecked")
    final Class<Object> dataTypeClass = (Class<Object>)dataType.getJavaClass();
    final StringConverter<Object> converter = StringConverterRegistry.getInstance()
      .getConverter(dataTypeClass);
    if (converter == null) {
      out.text(value);
    } else {
      final String stringValue = converter.toString(value);
      out.text(stringValue);
    }
  }
}
