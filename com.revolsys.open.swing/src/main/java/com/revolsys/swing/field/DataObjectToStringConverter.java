package com.revolsys.swing.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.data.record.Record;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.Property;

public class DataObjectToStringConverter extends ObjectToStringConverter {

  private final List<String> attributeNames;

  public DataObjectToStringConverter(final List<String> attributeNames) {
    super();
    this.attributeNames = attributeNames;
  }

  public DataObjectToStringConverter(final String... attributeNames) {
    this(Arrays.asList(attributeNames));
  }

  @Override
  public String getPreferredStringForItem(final Object value) {
    if (value == null || value == CodeTableComboBoxModel.NULL) {
      return "";
    } else if (value instanceof Record) {
      final Record object = (Record)value;
      final List<String> values = new ArrayList<String>();
      for (final String attributeName : this.attributeNames) {
        final String text = StringConverterRegistry.toString(Property.get(
          object, attributeName));
        values.add(text);
      }
      return CollectionUtil.toString(values);

    } else {
      return StringConverterRegistry.toString(value);
    }
  }
}
