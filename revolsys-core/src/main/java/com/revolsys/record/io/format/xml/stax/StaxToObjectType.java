package com.revolsys.record.io.format.xml.stax;

import java.util.function.Function;

import com.revolsys.record.io.format.xml.XmlName;
import com.revolsys.record.io.format.xml.XmlNamespace;
import com.revolsys.record.io.format.xml.XmlType;

public class StaxToObjectType<T> extends XmlType {

  public static <T2> StaxToObjectType<T2> create(final XmlNamespace namespace,
    final String localPart, final Function<StaxReader, T2> reader) {
    return new StaxToObjectType<>(namespace.getName(localPart), reader);
  }

  private Function<StaxReader, T> reader;

  public StaxToObjectType(final XmlName xmlName, final Function<StaxReader, T> reader) {
    super(xmlName);
    this.reader = reader;
  }

  public StaxToObjectType(final XmlNamespace namespace, final String localPart,
    final Function<StaxReader, T> reader) {
    this(namespace.getName(localPart), reader);
  }

  public T read(final StaxReader in) {
    return this.reader.apply(in);
  }

}
