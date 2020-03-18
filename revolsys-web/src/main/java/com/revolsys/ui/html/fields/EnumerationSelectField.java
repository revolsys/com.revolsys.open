package com.revolsys.ui.html.fields;

import com.revolsys.util.CaseConverter;

public class EnumerationSelectField extends SelectField {

  private Class<? extends Enum<?>> enumClass;

  public EnumerationSelectField() {
  }

  @Override
  public EnumerationSelectField clone() {
    final EnumerationSelectField field = new EnumerationSelectField();
    field.setName(getName());
    field.setDefaultValue(getDefaultValue());
    field.setRequired(isRequired());
    field.setReadOnly(isReadOnly());
    field.setNullValueLabel(getNullValueLabel());
    field.setEnumClass(this.enumClass);
    return field;
  }

  public Class<? extends Enum<?>> getEnumClass() {
    return this.enumClass;
  }

  public void setEnumClass(final Class<? extends Enum<?>> enumClass) {
    this.enumClass = enumClass;
    final Enum<?>[] enumConstants = enumClass.getEnumConstants();
    for (final Enum<?> enumValue : enumConstants) {
      final String name = enumValue.name();
      final String label = CaseConverter.toCapitalizedWords(name);
      addOption(enumValue, label);
    }
  }
}
