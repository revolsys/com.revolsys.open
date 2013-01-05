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

import com.revolsys.converter.string.StringConverterRegistry;

public class SortableTableCellHeaderRenderer extends DefaultTableCellRenderer
  implements UIResource {
  private static final long serialVersionUID = 1L;

  private class EmptyIcon implements Icon, Serializable {

    private static final long serialVersionUID = 1L;

    int height;

    int width;

    private EmptyIcon() {
      width = 0;
      height = 0;
    }

    public int getIconHeight() {
      return height;
    }

    public int getIconWidth() {
      return width;
    }

    public void paintIcon(Component component, Graphics g, int i, int j) {
    }

  }

  public static SortOrder getColumnSortOrder(JTable table, int column) {
    if (table != null) {
      RowSorter<? extends TableModel> rowSorter = table.getRowSorter();
      if (rowSorter != null) {
        List<? extends SortKey> list = rowSorter.getSortKeys();
        if (list.size() > 0) {
          SortKey sortKey = list.get(0);
          if (sortKey.getColumn() == table.convertColumnIndexToModel(column)) {
            return list.get(0).getSortOrder();
          }
        }
      } else if (table.getModel() instanceof SortableTableModel) {
        SortableTableModel sortableModel = (SortableTableModel)table.getModel();
        return sortableModel.getSortOrder(column);
      }
    }
    return null;
  }

  private EmptyIcon emptyIcon;

  private boolean horizontalTextPositionSet;

  private Icon sortArrow;

  public SortableTableCellHeaderRenderer() {
    emptyIcon = new EmptyIcon();
    setHorizontalAlignment(0);
  }

  private Point computeIconPosition(Graphics g) {
    java.awt.FontMetrics fontmetrics = g.getFontMetrics();
    Rectangle rectangle = new Rectangle();
    Rectangle rectangle1 = new Rectangle();
    Rectangle rectangle2 = new Rectangle();
    Insets insets = getInsets();
    rectangle.x = insets.left;
    rectangle.y = insets.top;
    rectangle.width = getWidth() - (insets.left + insets.right);
    rectangle.height = getHeight() - (insets.top + insets.bottom);
    SwingUtilities.layoutCompoundLabel(this, fontmetrics, getText(), sortArrow,
      getVerticalAlignment(), getHorizontalAlignment(),
      getVerticalTextPosition(), getHorizontalTextPosition(), rectangle,
      rectangle2, rectangle1, getIconTextGap());
    int i = getWidth() - insets.right - sortArrow.getIconWidth();
    int j = rectangle2.y;
    return new Point(i, j);
  }

  public Component getTableCellRendererComponent(JTable table, Object value,
    boolean isSelected, boolean hasFocus, int row, int column) {
    Icon icon = (Icon)UIManager.get("Table.naturalSortIcon");
    boolean isPrint = false;
    if (table != null) {
      JTableHeader header = table.getTableHeader();
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
        if (!horizontalTextPositionSet) {
          setHorizontalTextPosition(10);
        }
        SortOrder sortorder = getColumnSortOrder(table, column);

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
    String text = StringConverterRegistry.toString(value);
    setText(text);
    setIcon(icon);
    sortArrow = icon;
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

  public void paintComponent(Graphics g) {
    boolean flag = UIManager.get("TableHeader.rightAlignSortArrow") == Boolean.TRUE;
    if (flag && sortArrow != null) {
      emptyIcon.width = sortArrow.getIconWidth();
      emptyIcon.height = sortArrow.getIconHeight();
      setIcon(emptyIcon);
      super.paintComponent(g);
      Point point = computeIconPosition(g);
      sortArrow.paintIcon(this, g, point.x, point.y);
    } else {
      super.paintComponent(g);
    }
  }

  public void setHorizontalTextPosition(int i) {
    horizontalTextPositionSet = true;
    super.setHorizontalTextPosition(i);
  }
}
