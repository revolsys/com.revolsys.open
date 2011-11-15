package com.revolsys.io.xml;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.NamedObject;
import com.revolsys.io.xml.io.XmlWriter;

public class XmlDataObjectWriter extends AbstractWriter<DataObject> {

  private DataObjectMetaData metaData;

  private XmlWriter out;

  boolean startAttribute;

  private boolean singleObject;

  private boolean opened;

  public XmlDataObjectWriter(final DataObjectMetaData metaData,
    final java.io.Writer out) {
    this.metaData = metaData;
    if (out instanceof XmlWriter) {
      this.out = (XmlWriter)out;
    } else {
      this.out = new XmlWriter(out);
    }
  }

  @Override
  public String toString() {
    return metaData.getName().toString();
  }

  public void write(final DataObject object) {
    if (!opened) {
      writeHeader();
    }
    out.startTag(object.getTypeName());

    final int attributeCount = metaData.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final String name = metaData.getAttributeName(i);
      final Object value = object.getValue(i);
      final QName tagName = new QName(name);
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, ?> map = (Map<String, ?>)value;
        out.startTag(tagName);
        map(map);
        out.endTag();
      } else if (value instanceof List) {
        List<?> list = (List<?>)value;
        out.startTag(tagName);
        list(list);
        out.endTag();
      } else {
        final DataType dataType = metaData.getAttributeType(i);
        final String string = StringConverterRegistry.toString(dataType, value);
        out.nillableElement(tagName, string);
      }
    }
    out.endTag();
  }

  @Override
  public void setProperty(String name, Object value) {
    super.setProperty(name, value);
    if (name.equals(IoConstants.INDENT_PROPERTY)) {
      out.setIndent((Boolean)value);
    }
  }

  /**
   * Closes the underlying reader.
   */
  public void close() {
    if (out != null) {
      try {
        if (opened) {
          if (!singleObject) {
            out.endTag();
          }
          out.endDocument();
        }
      } finally {
        FileUtil.closeSilent(out);
        out = null;
      }
    }
  }

  public void flush() {
    out.flush();
  }

  private void map(final Map<String, ? extends Object> values) {
    if (values instanceof NamedObject) {
      NamedObject namedObject = (NamedObject)values;
      out.startTag(new QName(namedObject.getName()));
    } else {
      out.startTag(new QName("item"));
    }

    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final Object key = field.getKey();
      final Object value = field.getValue();
      final QName tagName = new QName(key.toString());
      if (value instanceof Map) {
        Map<String, ?> map = (Map<String, ?>)value;
        out.startTag(tagName);
        map(map);
        out.endTag();
      } else if (value instanceof List) {
        List<?> list = (List<?>)value;
        out.startTag(tagName);
        list(list);
        out.endTag();
      } else {
        out.nillableElement(tagName, value);
      }
    }
    out.endTag();
  }

  private void list(List<? extends Object> list) {
    for (Object value : list) {
      if (value instanceof Map) {
        Map<String, ?> map = (Map<String, ?>)value;
        map(map);
      } else if (value instanceof List) {
        List<?> subList = (List<?>)value;
        list(subList);
      } else {
        out.startTag(new QName("item"));
        out.text(value);
        out.endTag();
      }
    }
  }

  private void writeHeader() {
    out.startDocument("UTF-8", "1.0");
    singleObject = Boolean.TRUE.equals(getProperty(IoConstants.SINGLE_OBJECT_PROPERTY));
    if (!singleObject) {
      out.startTag(new QName("items"));
    }
    opened = true;
  }
}
