package com.revolsys.swing.table.renderer;

import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.VerticalLayout;

public class MultiTableCellRenderer implements TableCellRenderer, MouseListener {

  private final JPanel panel = new JPanel();

  private final List<Object> renderers;

  public MultiTableCellRenderer(final Object... renderers) {
    if (renderers == null) {
      this.renderers = Collections.emptyList();
    } else {
      this.renderers = Arrays.asList(renderers);
    }
    this.panel.setOpaque(false);
    this.panel.setLayout(new VerticalLayout());
  }

  @Override
  public Component getTableCellRendererComponent(final JTable table, final Object value,
    final boolean isSelected, final boolean hasFocus, final int row, final int column) {
    this.panel.removeAll();
    for (final Object object : this.renderers) {
      Component component = null;
      if (object instanceof TableCellRenderer) {
        final TableCellRenderer renderer = (TableCellRenderer)object;
        component = renderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
          column);
      } else if (object instanceof Component) {
        component = (Component)object;
      }
      if (component != null) {
        this.panel.add(component);
      }
    }

    return this.panel;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    final MouseListener[] listeners = this.panel.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseClicked(e);
    }
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    final MouseListener[] listeners = this.panel.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseEntered(e);
    }
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    final MouseListener[] listeners = this.panel.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseExited(e);
    }
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    final MouseListener[] listeners = this.panel.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mousePressed(e);
    }
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    final MouseListener[] listeners = this.panel.getListeners(MouseListener.class);
    for (final MouseListener mouseListener : listeners) {
      mouseListener.mouseReleased(e);
    }
  }
}
