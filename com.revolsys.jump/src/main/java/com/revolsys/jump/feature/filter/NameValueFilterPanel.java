package com.revolsys.jump.feature.filter;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.jump.ui.swing.EditPanel;
import com.revolsys.jump.ui.swing.EditPanelDialog;
import com.revolsys.jump.ui.swing.ObjectEditPanel;
import com.revolsys.jump.ui.swing.ToolbarListSelectionListener;
import com.revolsys.jump.ui.swing.table.DeleteTableModelRowAction;
import com.revolsys.jump.ui.swing.table.ObjectTableModel;
import com.revolsys.jump.ui.swing.table.TableRowSelectedEnableCheck;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;

public class NameValueFilterPanel extends EditPanel<NameValueFeatureFilter> {

  /**
   * 
   */
  private static final long serialVersionUID = -6211138055130683218L;

  public final class AddTableModelRowAction implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      ObjectEditPanel panel = new ObjectEditPanel(PROPERTY_NAMES,
        PROPERTY_LABELS);
      panel.setValue(new NameValue());
      Window window = SwingUtilities.windowForComponent(getParent());

      EditPanelDialog dialog;
      if (window instanceof Frame) {
        dialog = new EditPanelDialog(workbenchContext, (Frame)window, panel);
      } else if (window instanceof JDialog) {
        dialog = new EditPanelDialog(workbenchContext, (JDialog)window, panel);
      } else {
        return;
      }
      dialog.setVisible(true);
      if (dialog.wasOKPressed()) {
        NameValue nameValue = (NameValue)dialog.getValue();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
          tableModel.addRow(nameValue);
        } else {
          tableModel.insertRow(selectedRow, nameValue);
        }
      } else {
        System.err.println("hello");
      }

    }
  }

  private static final String[] PROPERTY_NAMES = new String[] {
    "name", "operator", "value"
  };

  private static final String[] PROPERTY_LABELS = new String[] {
    "Name", "Operator", "Value"
  };

  private WorkbenchContext workbenchContext;

  private ObjectTableModel<NameValue> tableModel;

  private JTable table = new JTable();

  private EnableableToolBar toolBar = new EnableableToolBar();;

  public NameValueFilterPanel(final WorkbenchContext workbenchContext) {
    super(new BorderLayout());
    this.workbenchContext = workbenchContext;
    initTable();
    initToolbar();
  }

  private void initToolbar() {
    toolBar.add(new JButton(), "Add",
      GUIUtil.toSmallIcon(IconLoader.icon("Plus.gif")),
      new AddTableModelRowAction(), new MultiEnableCheck());
    toolBar.add(new JButton(), "Remove",
      GUIUtil.toSmallIcon(IconLoader.icon("Delete.gif")),
      new DeleteTableModelRowAction(table, tableModel),
      new TableRowSelectedEnableCheck(table));
    add(toolBar, BorderLayout.SOUTH);
    toolBar.updateEnabledState();
  }

  private void initTable() {
    tableModel = new ObjectTableModel<NameValue>(PROPERTY_NAMES, PROPERTY_LABELS);
    table.setModel(tableModel);
    table.createDefaultColumnsFromModel();

    table.setRowSelectionAllowed(true);
    table.getSelectionModel().addListSelectionListener(
      new ToolbarListSelectionListener(toolBar));

    TableColumnModel columnModel = table.getColumnModel();
    TableColumn operatorColumn = columnModel.getColumn(1);
    DefaultCellEditor operatorCellEditor = new DefaultCellEditor(
      new OperatorComboBox());
    operatorColumn.setCellEditor(operatorCellEditor);

    JScrollPane pane = new JScrollPane();
    pane.getViewport().add(table);
    add(pane, BorderLayout.CENTER);
  }

  public void setValue(final NameValueFeatureFilter filter) {
    super.setValue(filter);
    tableModel.setRows(filter.getValues());
  }

  public NameValueFeatureFilter getValue() {
    NameValueFeatureFilter filter = super.getValue();
    filter.setValues(tableModel.getRows());
    return filter;
  }

  public String getTitle() {
    return "Filter Expression";
  }
}
