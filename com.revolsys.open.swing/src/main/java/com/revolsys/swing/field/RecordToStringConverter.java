package com.revolsys.swing.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;
import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;

import com.revolsys.record.Record;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

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
    if (value == null || value == Identifier.NULL) {
      return "";
    } else if (value instanceof Record) {
      final Record object = (Record)value;
      final List<String> values = new ArrayList<>();
      for (final String fieldName : this.fieldNames) {
        final String text = DataTypes.toString(Property.get(object, fieldName));
        values.add(text);
      }
      return Strings.toString(values);

    } else {
      return DataTypes.toString(value);
    }
  }
}
