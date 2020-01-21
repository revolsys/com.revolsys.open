package com.revolsys.swing.table.json;

import java.awt.Component;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.common.number.Numbers;

import com.revolsys.swing.SwingUtil;

public class JsonObjectTableCellRenderer implements TableCellRenderer {
  private final JLabel labelComponent = new JLabel();

  private final JLabel valueComponent = new JLabel();

  public JsonObjectTableCellRenderer() {
    this.labelComponent.setFont(SwingUtil.BOLD_FONT);
    this.labelComponent.setOpaque(true);

    this.valueComponent.setFont(SwingUtil.FONT);
    this.valueComponent.setOpaque(true);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, int rowIndex, int columnIndex) {
    if (table instanceof JXTable) {
      final JXTable jxTable = (JXTable)table;
      rowIndex = jxTable.convertRowIndexToModel(rowIndex);
      columnIndex = jxTable.convertColumnIndexToModel(columnIndex);
    }
    final JsonObjectTableModel model = (JsonObjectTableModel)table.getModel();

    JComponent component = null;
    this.valueComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
    if (columnIndex == 0) {
      this.valueComponent.setText(String.valueOf(rowIndex + 1));
      this.valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
      this.valueComponent.setHorizontalTextPosition(SwingConstants.RIGHT);
      component = this.valueComponent;
    } else if (columnIndex == 1) {
      final String title = model.getPropertyTitle(rowIndex);
      this.labelComponent.setText(title);
      this.labelComponent.setBorder(new EmptyBorder(1, 2, 1, 2));
      component = this.labelComponent;
    } else {
      final String text = DataTypes.toString(model.getValueAt(rowIndex, columnIndex));
      this.valueComponent.setText(text);
      if (Numbers.isNumber(text)) {
        this.valueComponent.setHorizontalAlignment(SwingConstants.RIGHT);
        this.valueComponent.setHorizontalTextPosition(SwingConstants.RIGHT);
      } else {
        this.valueComponent.setHorizontalAlignment(SwingConstants.LEFT);
        this.valueComponent.setHorizontalTextPosition(SwingConstants.LEFT);
      }
      component = this.valueComponent;
    }
    component.setToolTipText(null);
    return component;
  }

}
