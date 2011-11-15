package com.revolsys.xhtml;

import java.io.Writer;
import java.net.URI;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.HtmlUtil;
import com.revolsys.xml.io.XmlWriter;

public class XhtmlDataObjectWriter extends AbstractWriter<DataObject> {

  private boolean opened = false;

  /** The writer */
  private XmlWriter out;

  private String title = "Items";

  private boolean wrap = true;

  private DataObjectMetaData metaData;

  public XhtmlDataObjectWriter(DataObjectMetaData metaData, final Writer out) {
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

  public void setProperty(final String name, final Object value) {
    if (name.equals(IoConstants.WRAP_PROPERTY)) {
      wrap = Boolean.valueOf(value.toString());
    } else if (name.equals(IoConstants.TITLE_PROPERTY)) {
      title = value.toString();
    }
    super.setProperty(name, value);
  }

  public void write(DataObject object) {
    if (!opened) {
      if (title == null) {
        String name = object.getTypeName().getLocalPart();
        if (name != null) {
          this.title = name;
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

    final int attributeCount = metaData.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final String name = metaData.getAttributeName(i);
      final Object value = object.getValue(i);
      out.startTag(HtmlUtil.TR);
      // TODO case converter on key name
      out.element(HtmlUtil.TH, CaseConverter.toCapitalizedWords(name));
      out.startTag(HtmlUtil.TD);
      if (value instanceof URI) {
        HtmlUtil.serializeA(out, null, value, value);
      } else {
        final DataType dataType = metaData.getAttributeType(i);
        String string = StringConverterRegistry.toString(dataType, value);
        out.text(string);
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
