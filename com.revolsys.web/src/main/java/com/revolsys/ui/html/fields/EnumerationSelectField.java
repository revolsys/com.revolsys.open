package com.revolsys.ui.html.fields;

import com.revolsys.util.CaseConverter;

public class EnumerationSelectField extends SelectField {

  private Class<? extends Enum<?>> enumClass;

  public EnumerationSelectField() {
  }

  public EnumerationSelectField clone() {
    EnumerationSelectField field = new EnumerationSelectField();
    field.setName(getName());
    field.setDefaultValue(getDefaultValue());
    field.setRequired(isRequired());
    field.setReadOnly(isReadOnly());
    field.setNullValueLabel(getNullValueLabel());
    field.setEnumClass(enumClass);
    return field;
  }

  public Class<? extends Enum<?>> getEnumClass() {
    return enumClass;
  }

  public void setEnumClass(Class<? extends Enum<?>> enumClass) {
    this.enumClass = enumClass;
    Enum<?>[] enumConstants = enumClass.getEnumConstants();
    for (Enum<?> enumValue : enumConstants) {
      String name = enumValue.name();
      String label = CaseConverter.toCapitalizedWords(name);
      addOption(enumValue, label);
    }
  }
}
