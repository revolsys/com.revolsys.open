package com.revolsys.swing.table;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.RowSorter.SortKey;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.plaf.UIResource;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;

import org.jeometry.common.data.type.DataTypes;

import com.revolsys.util.Booleans;

public class SortableTableCellHeaderRenderer extends DefaultTableCellRenderer
  implements UIResource {
  private class EmptyIcon implements Icon, Serializable {

    private static final long serialVersionUID = 1L;

    int height;

    int width;

    private EmptyIcon() {
      this.width = 0;
      this.height = 0;
    }

    @Override
    public int getIconHeight() {
      return this.height;
    }

    @Override
    public int getIconWidth() {
      return this.width;
    }

    @Override
    public void paintIcon(final Component component, final Graphics g, final int i, final int j) {
    }

  }

  private static final long serialVersionUID = 1L;

  public static SortOrder getColumnSortOrder(final JTable table, final int column) {
    if (table != null) {
      final RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
      if (rowSorter != null) {
        final List<? extends SortKey> list = rowSorter.getSortKeys();
        if (list.size() > 0) {
          final SortKey sortKey = list.get(0);
          if (sortKey.getColumn() == table.convertColumnIndexToModel(column)) {
            return list.get(0).getSortOrder();
          }
        }
      } else if (table.getModel() instanceof SortableTableModel) {
        final SortableTableModel sortableModel = (SortableTableModel)table.getModel();
        return sortableModel.getSortOrder(column);
      }
    }
    return null;
  }

  private final EmptyIcon emptyIcon;

  private boolean horizontalTextPositionSet;

  private Icon sortArrow;

  public SortableTableCellHeaderRenderer() {
    this.emptyIcon = new EmptyIcon();
    setHorizontalAlignment(0);
  }

  private Point computeIconPosition(final Graphics g) {
    final java.awt.FontMetrics fontmetrics = g.getFontMetrics();
    final Rectangle rectangle = new Rectangle();
    final Rectangle rectangle1 = new Rectangle();
    final Rectangle rectangle2 = new Rectangle();
    final Insets insets = getInsets();
    rectangle.x = insets.left;
    rectangle.y = insets.top;
    rectangle.width = getWidth() - (insets.left + insets.right);
    rectangle.height = getHeight() - (insets.top + insets.bottom);
    SwingUtilities.layoutCompoundLabel(this, fontmetrics, getText(), this.sortArrow,
      getVerticalAlignment(), getHorizontalAlignment(), getVerticalTextPosition(),
      getHorizontalTextPosition(), rectangle, rectangle2, rectangle1, getIconTextGap());
    final int i = getWidth() - insets.right - this.sortArrow.getIconWidth();
    final int j = rectangle2.y;
    return new Point(i, j);
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    Icon icon = (Icon)UIManager.get("Table.naturalSortIcon");
    boolean isPrint = false;
    if (table != null) {
      final JTableHeader header = table.getTableHeader();
      if (header != null) {
        Color foreground = null;
        Color background = null;
        if (hasFocus) {
          foreground = (Color)UIManager.get("TableHeader.focusCellForeground");
          background = (Color)UIManager.get("TableHeader.focusCellBackground");
        }
        if (foreground == null) {
          foreground = header.getForeground();
        }
        if (background == null) {
          background = header.getBackground();
        }
        setForeground(foreground);
        setBackground(background);
        setFont(header.getFont());
        isPrint = header.isPaintingForPrint();
      }
      if (!isPrint) {
        if (!this.horizontalTextPositionSet) {
          setHorizontalTextPosition(10);
        }
        final SortOrder sortorder = getColumnSortOrder(table, column);

        if (sortorder != null) {
          switch (sortorder) {
            case ASCENDING:
              icon = (Icon)UIManager.get("Table.ascendingSortIcon");
            break;

            case DESCENDING:
              icon = (Icon)UIManager.get("Table.descendingSortIcon");
            break;

            case UNSORTED:
              icon = (Icon)UIManager.get("Table.naturalSortIcon");
            break;
          }
        }
      }
    }
    final String text = DataTypes.toString(value);
    setText(text);
    setIcon(icon);
    this.sortArrow = icon;
    Border border = null;
    if (hasFocus) {
      border = (Border)UIManager.get("TableHeader.focusCellBorder");
    }
    if (border == null) {
      border = (Border)UIManager.get("TableHeader.cellBorder");
    }
    setBorder(border);
    return this;
  }

  @Override
  public void paintComponent(final Graphics g) {
    final boolean flag = Booleans.getBoolean(UIManager.get("TableHeader.rightAlignSortArrow"));
    if (flag && this.sortArrow != null) {
      this.emptyIcon.width = this.sortArrow.getIconWidth();
      this.emptyIcon.height = this.sortArrow.getIconHeight();
      setIcon(this.emptyIcon);
      super.paintComponent(g);
      final Point point = computeIconPosition(g);
      this.sortArrow.paintIcon(this, g, point.x, point.y);
    } else {
      super.paintComponent(g);
    }
  }

  @Override
  public void setHorizontalTextPosition(final int i) {
    this.horizontalTextPositionSet = true;
    super.setHorizontalTextPosition(i);
  }
}
