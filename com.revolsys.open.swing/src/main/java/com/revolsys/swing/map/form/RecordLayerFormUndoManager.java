package com.revolsys.swing.map.form;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.undo.UndoManager;

public class RecordLayerFormUndoManager extends UndoManager {
  private static final long serialVersionUID = 1L;

  private final Reference<LayerRecordForm> form;

  public RecordLayerFormUndoManager(final LayerRecordForm form) {
    this.form = new WeakReference<>(form);
  }

  @Override
  public void redo() {
    final LayerRecordForm form = this.form.get();
    if (form != null) {
      final boolean validationEnabled = form.isFieldValidationEnabled();
      try (
        final BaseCloseable c = form.setFieldValidationEnabled(false)) {
        super.redo();
        if (validationEnabled) {
          form.validateFields(form.getFieldsToValidate());
        }
      }
    }
  }

  @Override
  public void undo() {
    final LayerRecordForm form = this.form.get();
    if (form != null) {
      final boolean validationEnabled = form.isFieldValidationEnabled();
      try (
        final BaseCloseable c = form.setFieldValidationEnabled(false)) {
        super.undo();
        if (validationEnabled) {
          form.validateFields(form.getFieldsToValidate());
        }
      }
    }
  }
}
