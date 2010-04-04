package com.revolsys.jump.ui.style;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;

import com.revolsys.jump.ui.swing.EditPanel;
import com.revolsys.jump.ui.swing.EditPanelDialog;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.ui.GUIUtil;

public class EditPanelDialogTableCellEditor<T> extends AbstractCellEditor
  implements TableCellEditor, ActionListener {
  private static final long serialVersionUID = 5585261157357073529L;

  private static final String EDIT = "edit";

  private EditPanelDialog<T> dialog;

  private JButton button;

  private WorkbenchContext workbenchContext;

  private EditPanel<T> panel;

  public EditPanelDialogTableCellEditor(
    final WorkbenchContext workbenchContext, final EditPanel<T> panel) {
    this.workbenchContext = workbenchContext;
    button = new JButton();
    button.setActionCommand(EDIT);
    button.addActionListener(this);
    button.setBorderPainted(false);
    this.panel = panel;

  }

  @SuppressWarnings("unchecked")
  public Component getTableCellEditorComponent(final JTable table,
    final Object value, final boolean isSelected, final int row,
    final int column) {
    if (dialog == null) {
      dialog = new EditPanelDialog<T>(workbenchContext,
        (JDialog)SwingUtilities.windowForComponent(table), panel);
    }
    dialog.setValue((T)value);
    return button;
  }

  public Object getCellEditorValue() {
    return panel.getValue();
  }

  public void actionPerformed(final ActionEvent e) {
    if (e.getSource() == button) {
      GUIUtil.centreOnWindow(dialog);
      dialog.setVisible(true);
      fireEditingStopped();
    }
  }
}
