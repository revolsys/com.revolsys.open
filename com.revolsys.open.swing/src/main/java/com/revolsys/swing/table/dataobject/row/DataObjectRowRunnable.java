package com.revolsys.swing.table.dataobject.row;

import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.process.InvokeMethodRunnable;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.action.enablecheck.EnableCheck;
import com.revolsys.swing.table.TablePanel;
import com.revolsys.swing.table.dataobject.model.DataObjectRowTableModel;

public class DataObjectRowRunnable extends InvokeMethodRunnable {

  public static InvokeMethodAction createAction(final CharSequence name,
    final Icon icon, final EnableCheck enableCheck, final String methodName,
    final Object... parameters) {
    final DataObjectRowRunnable runnable = new DataObjectRowRunnable(
      methodName, parameters);
    final InvokeMethodAction action = new InvokeMethodAction(name,
      name.toString(), icon, true, runnable);
    action.setEnableCheck(enableCheck);

    return action;
  }

  public static InvokeMethodAction createAction(final CharSequence name,
    final String iconName, final EnableCheck enableCheck,
    final String methodName, final Object... parameters) {
    final ImageIcon icon = SilkIconLoader.getIcon(iconName);
    return createAction(name, icon, enableCheck, methodName, parameters);
  }

  protected DataObjectRowRunnable(final String methodName,
    final Object[] parameters) {
    super(methodName, parameters);
  }

  @Override
  public Object getObject() {
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

}
