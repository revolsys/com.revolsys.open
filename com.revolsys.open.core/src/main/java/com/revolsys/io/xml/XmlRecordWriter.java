package com.revolsys.io.xml;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.data.record.property.RecordProperties;
import com.revolsys.data.record.schema.RecordDefinition;
import com.revolsys.data.types.DataType;
import com.revolsys.io.AbstractWriter;
import com.revolsys.io.FileUtil;
import com.revolsys.io.IoConstants;
import com.revolsys.io.NamedObject;

public class XmlRecordWriter extends AbstractWriter<Record> {

  private final RecordDefinition recordDefinition;

  private XmlWriter out;

  boolean startAttribute;

  private boolean singleObject;

  private boolean opened;

  public XmlRecordWriter(final RecordDefinition recordDefinition,
    final java.io.Writer out) {
    this.recordDefinition = recordDefinition;
    if (out instanceof XmlWriter) {
      this.out = (XmlWriter)out;
    } else {
      this.out = new XmlWriter(out);
    }
  }

  /**
   * Closes the underlying reader.
   */
  @Override
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

  @Override
  public void flush() {
    out.flush();
  }

  private void list(final List<? extends Object> list) {
    for (final Object value : list) {
      if (value instanceof Map) {
        final Map<String, ?> map = (Map<String, ?>)value;
        map(map);
      } else if (value instanceof List) {
        final List<?> subList = (List<?>)value;
        list(subList);
      } else {
        out.startTag(new QName("item"));
        out.text(value);
        out.endTag();
      }
    }
  }

  private void map(final Map<String, ? extends Object> values) {
    if (values instanceof NamedObject) {
      final NamedObject namedObject = (NamedObject)values;
      out.startTag(new QName(namedObject.getName()));
    } else {
      out.startTag(new QName("item"));
    }

    for (final Entry<String, ? extends Object> field : values.entrySet()) {
      final Object key = field.getKey();
      final Object value = field.getValue();
      final QName tagName = new QName(key.toString());
      if (value instanceof Map) {
        final Map<String, ?> map = (Map<String, ?>)value;
        out.startTag(tagName);
        map(map);
        out.endTag();
      } else if (value instanceof List) {
        final List<?> list = (List<?>)value;
        out.startTag(tagName);
        list(list);
        out.endTag();
      } else {
        out.nillableElement(tagName, value);
      }
    }
    out.endTag();
  }

  @Override
  public void setProperty(final String name, final Object value) {
    super.setProperty(name, value);
    if (name.equals(IoConstants.INDENT_PROPERTY)) {
      out.setIndent((Boolean)value);
    }
  }

  @Override
  public String toString() {
    return recordDefinition.getPath().toString();
  }

  @Override
  public void write(final Record object) {
    if (!opened) {
      writeHeader();
    }
    QName qualifiedName = recordDefinition.getProperty(RecordProperties.QUALIFIED_NAME);
    if (qualifiedName == null) {
      qualifiedName = new QName(recordDefinition.getTypeName());
    }

    out.startTag(qualifiedName);

    final int attributeCount = recordDefinition.getAttributeCount();
    for (int i = 0; i < attributeCount; i++) {
      final String name = recordDefinition.getAttributeName(i);
      final Object value = object.getValue(i);
      final QName tagName = new QName(name);
      if (value instanceof Map) {
        @SuppressWarnings("unchecked")
        final Map<String, ?> map = (Map<String, ?>)value;
        out.startTag(tagName);
        map(map);
        out.endTag();
      } else if (value instanceof List) {
        final List<?> list = (List<?>)value;
        out.startTag(tagName);
        list(list);
        out.endTag();
      } else {
        final DataType dataType = recordDefinition.getAttributeType(i);
        final String string = StringConverterRegistry.toString(dataType, value);
        out.nillableElement(tagName, string);
      }
    }
    out.endTag();
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
