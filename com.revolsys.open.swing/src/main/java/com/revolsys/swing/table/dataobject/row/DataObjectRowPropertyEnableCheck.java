package com.revolsys.swing.table.dataobject.row;

import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.action.enablecheck.AbstractEnableCheck;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.model.DataObjectRowTableModel;
import com.revolsys.util.JavaBeanUtil;

public class DataObjectRowPropertyEnableCheck extends AbstractEnableCheck {
  private final String propertyName;

  private final Object value;

  private boolean invert = false;

  public DataObjectRowPropertyEnableCheck(final boolean invert,
    final String propertyName) {
    this(invert, propertyName, true);
  }

  public DataObjectRowPropertyEnableCheck(final boolean invert,
    final String propertyName, final Object value) {
    this.invert = invert;
    this.propertyName = propertyName;
    this.value = value;
  }

  public DataObjectRowPropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public DataObjectRowPropertyEnableCheck(final String propertyName,
    final Object value) {
    this(false, propertyName, value);
  }

  private DataObject getObject() {
    final DataObjectRowTable table = TablePanel.getEventTable();
    if (table != null) {
      final int eventRow = TablePanel.getEventRow();
      if (eventRow != -1) {
        final DataObjectRowTableModel model = (DataObjectRowTableModel)table.getModel();
        final DataObject object = model.getRecord(eventRow);
        return object;
      }
    }
    return null;
  }

  @Override
  public boolean isEnabled() {
    try {
      final DataObject object = getObject();
      final Object value = JavaBeanUtil.getSimpleProperty(object,
        this.propertyName);
      final boolean equal = EqualsRegistry.equal(value, this.value);
      if (equal) {
        if (invert) {
          return disabled();
        } else {
          return enabled();
        }
      } else {
        if (invert) {
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
