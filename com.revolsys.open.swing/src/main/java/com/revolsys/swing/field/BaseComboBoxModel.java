package com.revolsys.swing.field;

import javax.swing.ComboBoxModel;

public interface BaseComboBoxModel<V> extends ComboBoxModel<V> {
  default ComboBox<V> newComboBox(final String fieldName) {
    return ComboBox.newComboBox(fieldName, this);
  }
}
