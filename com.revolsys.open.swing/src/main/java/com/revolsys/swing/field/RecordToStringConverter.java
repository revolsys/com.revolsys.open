package com.revolsys.swing.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class RecordToStringConverter extends ObjectToStringConverter {

  private final List<String> fieldNames;

  public RecordToStringConverter(final List<String> fieldNames) {
    super();
    this.fieldNames = fieldNames;
  }

  public RecordToStringConverter(final String... fieldNames) {
    this(Arrays.asList(fieldNames));
  }

  @Override
  public String getPreferredStringForItem(final Object value) {
    if (value == null || value == CodeTableComboBoxModel.NULL) {
      return "";
    } else if (value instanceof Record) {
      final Record object = (Record)value;
      final List<String> values = new ArrayList<String>();
      for (final String fieldName : this.fieldNames) {
        final String text = StringConverterRegistry.toString(Property.get(object, fieldName));
        values.add(text);
      }
      return CollectionUtil.toString(values);

    } else {
      return StringConverterRegistry.toString(value);
    }
  }
}
