package com.revolsys.swing.map.form;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import org.jeometry.common.data.type.DataType;

import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.swing.map.layer.record.LayerRecord;
import com.revolsys.util.Property;

public class LayerFormRecordPropertyEnableCheck extends AbstractEnableCheck {
  private final Reference<LayerRecordForm> form;

  private boolean inverse = false;

  private final String propertyName;

  private final Object value;

  public LayerFormRecordPropertyEnableCheck(final LayerRecordForm form, final String propertyName) {
    this(form, propertyName, true);
  }

  public LayerFormRecordPropertyEnableCheck(final LayerRecordForm form, final String propertyName,
    final Object value) {
    this(form, propertyName, value, false);
  }

  public LayerFormRecordPropertyEnableCheck(final LayerRecordForm form, final String propertyName,
    final Object value, final boolean inverse) {
    this.form = new WeakReference<>(form);
    Property.addListener(form, propertyName, this);
    this.propertyName = propertyName;
    this.value = value;
    this.inverse = inverse;
  }

  protected LayerRecord getRecord() {
    final LayerRecordForm form = this.form.get();
    if (form == null) {
      return null;
    } else {
      return form.getRecord();
    }
  }

  @Override
  public boolean isEnabled() {
    final LayerRecord record = getRecord();
    final Object value = Property.getSimple(record, this.propertyName);
    final boolean equal = DataType.equal(value, this.value);
    if (equal == !this.inverse) {
      return enabled();
    } else {
      return disabled();
    }
  }

  @Override
  public String toString() {
    return getRecord() + "." + this.propertyName + "=" + this.value;
  }
}
