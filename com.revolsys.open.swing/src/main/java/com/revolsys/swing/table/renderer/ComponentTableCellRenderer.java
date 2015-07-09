package com.revolsys.swing.table.renderer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ComponentTableCellRenderer implements TableCellRenderer, MouseListener {

  private JComponent component;

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    if (this.component instanceof TableCellRenderer) {
      final TableCellRenderer renderer = (TableCellRenderer)this.component;
      return renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
        column);
    } else {
      return this.component;
    }
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final MouseListener[] listeners = this.component.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseClicked(e);
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    final MouseListener[] listeners = this.component.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseEntered(e);
    }
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    final MouseListener[] listeners = this.component.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseExited(e);
    }
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final MouseListener[] listeners = this.component.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mousePressed(e);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    final MouseListener[] listeners = this.component.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseReleased(e);
    }
  }
}
