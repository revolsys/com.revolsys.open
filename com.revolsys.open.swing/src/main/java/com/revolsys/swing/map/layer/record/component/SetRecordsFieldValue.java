package com.revolsys.swing.map.layer.record.component;

import java.awt.Component;

import javax.swing.JPanel;

import org.jeometry.common.awt.WebColors;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.util.Property;

public class SetRecordsFieldValue extends AbstractUpdateField {
  private static final long serialVersionUID = 1L;

  public static void addMenuItem(final MenuFactory headerMenu) {
    final EnableCheck enableCheck = newEnableCheck();
    headerMenu.addMenuItem("field", "Set Field Value", "table_fill_down", enableCheck,
      SetRecordsFieldValue::showDialog);
  }

  private static void showDialog() {
    final AbstractUpdateField dialog = new SetRecordsFieldValue();
    dialog.setVisible(true);
  }

  private Field editField;

  private SetRecordsFieldValue() {
    super("Set Field Value");

  }

  @Override
  protected String getProgressMonitorNote() {
    final FieldDefinition fieldDefinition = getFieldDefinition();
    return "Set " + fieldDefinition.getName() + "="
      + fieldDefinition.getCodeValue(this.editField.getFieldValue());
  }

  @Override
  protected JPanel initFieldPanel() {
    final FieldDefinition fieldDefinition = getFieldDefinition();

    final JPanel fieldPanel = new JPanel();
    final String fieldName = fieldDefinition.getName();
    final AbstractRecordLayer layer = getLayer();
    this.editField = layer.newFormField(fieldName, true);
    Property.addListenerNewValue(this.editField, fieldName, (newValue) -> {
      validateField(this.editField, fieldDefinition);
    });
    Property.addListenerNewValue(this.editField, "text", (newValue) -> {
      validateField(this.editField, fieldDefinition);
    });
    fieldPanel.add((Component)this.editField);
    GroupLayouts.makeColumns(fieldPanel, 1, true);
    return fieldPanel;
  }

  @Override
  public void updateRecord(final LayerRecord record) {
    final String fieldName = getFieldDefinition().getName();
    final Object value = this.editField.getFieldValue();
    final AbstractRecordLayer layer = record.getLayer();
    layer.setRecordValue(record, fieldName, value);
  }

  public void validateField(final Field field, final FieldDefinition fieldDefinition) {
    boolean valid = true;
    field.setFieldValid();
    final Object value = field.getFieldValue();
    try {
      fieldDefinition.validate(value);
    } catch (final IllegalArgumentException e) {
      field.setFieldInvalid(e.getMessage(), WebColors.Red, WebColors.Pink);
      valid = false;
    }
    setFormValid(valid);
  }
}
