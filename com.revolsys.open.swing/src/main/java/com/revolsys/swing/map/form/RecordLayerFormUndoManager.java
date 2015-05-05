package com.revolsys.swing.map.form;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.swing.undo.UndoManager;

public class RecordLayerFormUndoManager extends UndoManager {
  private static final long serialVersionUID = 1L;

  private final Reference<RecordLayerForm> form;

  public RecordLayerFormUndoManager(final RecordLayerForm form) {
    this.form = new WeakReference<RecordLayerForm>(form);
  }

  @Override
  public void redo() {
    final RecordLayerForm form = this.form.get();
    if (form != null) {
      final boolean validationEnabled = form.setFieldValidationEnabled(false);
      try {
        super.redo();
      } finally {
        if (validationEnabled) {
          form.validateFields(form.getFieldsToValidate());
        }
        form.setFieldValidationEnabled(validationEnabled);
      }
    }
  }

  @Override
  public void undo() {
    final RecordLayerForm form = this.form.get();
    if (form != null) {
      final boolean validationEnabled = form.setFieldValidationEnabled(false);
      try {
        super.undo();
      } finally {
        if (validationEnabled) {
          form.validateFields(form.getFieldsToValidate());
        }
        form.setFieldValidationEnabled(validationEnabled);
      }
    }
  }

}
