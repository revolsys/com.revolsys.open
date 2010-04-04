package com.revolsys.jump.ui.style;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import com.revolsys.jump.feature.filter.NameValueFeatureFilter;
import com.revolsys.jump.feature.filter.NameValueFilterPanel;
import com.revolsys.jump.ui.swing.EditPanel;
import com.revolsys.jump.ui.swing.EditPanelDialog;
import com.revolsys.jump.ui.swing.ToolbarListSelectionListener;
import com.revolsys.jump.ui.swing.table.DeleteTableModelRowAction;
import com.revolsys.jump.ui.swing.table.ObjectTableModel;
import com.revolsys.jump.ui.swing.table.TableRowSelectedEnableCheck;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.plugin.MultiEnableCheck;
import com.vividsolutions.jump.workbench.ui.EnableableToolBar;
import com.vividsolutions.jump.workbench.ui.GUIUtil;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyle;
import com.vividsolutions.jump.workbench.ui.renderer.style.BasicStyleListCellRenderer;
import com.vividsolutions.jump.workbench.ui.style.StylePanel;

public class FilterThemingStylePanel extends EditPanel<FilterThemingStyle>
  implements StylePanel {
  private static final long serialVersionUID = -4022456638862597855L;

  public static final int VISIBLE_COLUMN = 0;

  public static final int LABEL_COLUMN = 1;

  public static final int FILTER_COLUMN = 2;

  public static final int STYLE_COLUMN = 3;

  private Layer layer;

  private WorkbenchContext workbenchContext;

  private JTable table;

  private ObjectTableModel<FilterTheme> tableModel;

  private BasicStyleListCellRenderer basicStyleListCellRenderer = new BasicStyleListCellRenderer();

  private EnableableToolBar toolBar = new EnableableToolBar();

  private static final String[] PROPERTY_NAMES = new String[] {
    "visible", "label", "filter", "basicStyle"
  };

  private static final String[] PROPERTY_LABELS = new String[] {
    "Visible", "Label", "Filter", "Style"
  };

  public final class AddTableModelRowAction implements ActionListener {
    public void actionPerformed(final ActionEvent e) {
      FilterThemeEditPanel panel = new FilterThemeEditPanel(workbenchContext);
      panel.setValue(new FilterTheme());
      Window window = SwingUtilities.windowForComponent(getParent());

      EditPanelDialog<FilterTheme> dialog;
      if (window instanceof Frame) {
        dialog = new EditPanelDialog<FilterTheme>(workbenchContext,
          (Frame)window, panel);
      } else if (window instanceof JDialog) {
        dialog = new EditPanelDialog<FilterTheme>(workbenchContext,
          (JDialog)window, panel);
      } else {
        return;
      }
      dialog.setVisible(true);
      if (dialog.wasOKPressed()) {
        FilterTheme value = dialog.getValue();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
          tableModel.addRow(value);
        } else {
          tableModel.insertRow(selectedRow, value);
        }
      }

    }
  }

  public FilterThemingStylePanel(final Layer layer,
    final WorkbenchContext workbenchContext) {
    super(new BorderLayout());
    this.layer = layer;
    this.workbenchContext = workbenchContext;
    jbInit();
  }

  private void jbInit() {
    initTable(layer);
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

  private void initTable(final Layer layer) {
    tableModel = new ObjectTableModel<FilterTheme>(PROPERTY_NAMES,
      PROPERTY_LABELS);
    FilterThemingStyle filterStyle = (FilterThemingStyle)layer.getStyle(FilterThemingStyle.class);
    if (filterStyle == null) {
      filterStyle = new FilterThemingStyle(layer);
      layer.addStyle(filterStyle);
    }
    setValue(filterStyle);

    tableModel.setRows(filterStyle.getThemes());
    // tableModel = new FilterThemingTableModel(style, featureSchema);
    table = new JTable(tableModel);

    table.setRowSelectionAllowed(true);
    table.getSelectionModel().addListSelectionListener(
      new ToolbarListSelectionListener(toolBar));

    TableColumnModel columnModel = table.getColumnModel();
    TableColumn visibleColumn = columnModel.getColumn(VISIBLE_COLUMN);
    visibleColumn.setCellEditor(new DefaultCellEditor(new JCheckBox()));

    TableColumn styleColumn = columnModel.getColumn(STYLE_COLUMN);
    styleColumn.setCellRenderer(new TableCellRenderer() {
      public Component getTableCellRendererComponent(final JTable table,
        final Object value, final boolean isSelected, final boolean hasFocus,
        final int row, final int column) {
        JComponent renderer = (JComponent)basicStyleListCellRenderer.getListCellRendererComponent(
          new JList(), value, row, isSelected, hasFocus);

        return renderer;
      }
    });
    styleColumn.setCellEditor(new EditPanelDialogTableCellEditor<BasicStyle>(
      workbenchContext, new BasicStyleEditPanel(workbenchContext)));

    int colorWidth = 100;
    styleColumn.setPreferredWidth(colorWidth);
    styleColumn.setMinWidth(colorWidth);
    styleColumn.setMaxWidth(colorWidth);

    TableColumn filterColumn = columnModel.getColumn(FILTER_COLUMN);
    filterColumn.setCellRenderer(new DefaultTableCellRenderer());
    filterColumn.setCellEditor(new EditPanelDialogTableCellEditor<NameValueFeatureFilter>(
      workbenchContext, new NameValueFilterPanel(workbenchContext)));
    JScrollPane pane = new JScrollPane();
    pane.getViewport().add(table);
    add(pane, BorderLayout.CENTER);

  }

  public String getTitle() {
    return "Filter Theming";
  }

  public void setValue(final FilterThemingStyle style) {
    super.setValue(style);

    if (style != null) {
      List<FilterTheme> themes = style.getThemes();
      tableModel.setRows(themes);
    }

  }

  public void save() {
    FilterThemingStyle style = super.getValue();
    List<FilterTheme> themes = tableModel.getRows();
    if (style == null) {
      style = new FilterThemingStyle(layer);
      layer.addStyle(style);
      layer.getBasicStyle().setEnabled(false);
    }
    style.setThemes(themes);
    layer.fireAppearanceChanged();
  }

  public void updateStyles() {
    save();
  }

  public String validateInput() {
    return null;
  }

}
