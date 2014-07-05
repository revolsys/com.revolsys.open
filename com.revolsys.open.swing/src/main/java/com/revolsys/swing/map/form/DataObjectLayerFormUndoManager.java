package com.revolsys.swing.map.form;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import com.revolsys.swing.undo.UndoManager;

public class DataObjectLayerFormUndoManager extends UndoManager {
  private static final long serialVersionUID = 1L;

  private final Reference<LayerRecordForm> form;

  public DataObjectLayerFormUndoManager(final LayerRecordForm form) {
    this.form = new WeakReference<LayerRecordForm>(form);
  }

  @Override
  public void redo() {
    final LayerRecordForm form = this.form.get();
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
    final LayerRecordForm form = this.form.get();
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
