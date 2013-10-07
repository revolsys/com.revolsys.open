package com.revolsys.swing.table.dataobject.row;

import java.awt.Point;
import java.awt.event.MouseEvent;

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

  public DataObjectRowPropertyEnableCheck(final String propertyName) {
    this(propertyName, true);
  }

  public DataObjectRowPropertyEnableCheck(final String propertyName,
    final Object value) {
    this.propertyName = propertyName;
    this.value = value;
  }

  private DataObject getObject() {
    final MouseEvent event = TablePanel.getPopupMouseEvent();
    if (event == null) {
      return null;
    } else {
      final DataObjectRowTable table = (DataObjectRowTable)event.getSource();
      final Point point = event.getPoint();
      final int eventRow = table.rowAtPoint(point);

      final DataObjectRowTableModel model = (DataObjectRowTableModel)table.getModel();
      final DataObject object = model.getObject(eventRow);
      return object;
    }
  }

  @Override
  public boolean isEnabled() {
    try {
      final DataObject object = getObject();
      final Object value = JavaBeanUtil.getSimpleProperty(object,
        this.propertyName);
      if (EqualsRegistry.equal(value, this.value)) {
        return enabled();
      }
      return disabled();

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
