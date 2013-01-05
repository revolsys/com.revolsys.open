package com.revolsys.swing.field;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.util.CollectionUtil;
import com.revolsys.util.JavaBeanUtil;

public class DataObjectToStringConverter extends ObjectToStringConverter {

  private List<String> attributeNames;

  public DataObjectToStringConverter(List<String> attributeNames) {
    super();
    this.attributeNames = attributeNames;
  }

  public DataObjectToStringConverter(String... attributeNames) {
    this(Arrays.asList(attributeNames));
  }

  @Override
  public String getPreferredStringForItem(Object value) {
    if (value == null || value == CodeTableComboBoxModel.NULL) {
      return "";
    } else if (value instanceof DataObject) {
      DataObject object = (DataObject)value;
      List<String> values = new ArrayList<String>();
      for (String attributeName : attributeNames) {
        String text = StringConverterRegistry.toString(JavaBeanUtil.getValue(
          object, attributeName));
        values.add(text);
      }
      return CollectionUtil.toString(values);

    } else {
      return StringConverterRegistry.toString(value);
    }
  }
}
