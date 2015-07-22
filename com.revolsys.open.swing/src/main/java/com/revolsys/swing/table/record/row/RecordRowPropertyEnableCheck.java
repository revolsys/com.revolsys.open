package com.revolsys.swing.table.record.row;

import org.slf4j.LoggerFactory;

import com.revolsys.data.equals.Equals;
import com.revolsys.data.record.Record;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.record.model.RecordRowTableModel;
import com.revolsys.util.JavaBeanUtil;

public class RecordRowPropertyEnableCheck extends AbstractEnableCheck {
  private final String propertyName;

  private final Object value;

  private boolean invert = false;

  public RecordRowPropertyEnableCheck(final boolean invert, final String propertyName) {
    this(invert, propertyName, true);
  }

  public RecordRowPropertyEnableCheck(final boolean invert, final String propertyName,
    final Object value) {
    this.invert = invert;
    this.propertyName = propertyName;
    this.value = value;
  }

  public RecordRowPropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public RecordRowPropertyEnableCheck(final String propertyName, final Object value) {
    this(false, propertyName, value);
  }

  private Record getObject() {
    final RecordRowTable table = TablePanel.getEventTable();
    if (table != null) {
      final int eventRow = TablePanel.getEventRow();
      if (eventRow != -1) {
        final RecordRowTableModel model = (RecordRowTableModel)table.getModel();
        final Record record = model.getRecord(eventRow);
        return record;
      }
    }
    return null;
  }

  @Override
  public boolean isEnabled() {
    try {
      final Record object = getObject();
      final Object value = JavaBeanUtil.getSimpleProperty(object, this.propertyName);
      final boolean equal = Equals.equal(value, this.value);
      if (equal) {
        if (this.invert) {
          return disabled();
        } else {
          return enabled();
        }
      } else {
        if (this.invert) {
          return enabled();
        } else {
          return disabled();
        }
      }

    } catch (final Throwable e) {
      LoggerFactory.getLogger(getClass()).debug("Enable check not valid", e);
      return disabled();
    }
  }

  @Override
  public String toString() {
    return this.propertyName + "=" + this.value;
  }
}
