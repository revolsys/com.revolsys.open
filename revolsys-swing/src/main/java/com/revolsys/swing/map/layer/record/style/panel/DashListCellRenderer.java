package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class DashListCellRenderer extends DefaultListCellRenderer {
  private static final long serialVersionUID = 3249988648052455831L;

  private List<Double> dash;

  @SuppressWarnings("unchecked")
  @Override
  public Component getListCellRendererComponent(final JList<?> list, final Object value,
    final int index, final boolean isSelected, final boolean cellHasFocus) {
    super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    setText(null);
    this.dash = (List<Double>)value;
    setPreferredSize(new Dimension(100, 10));
    return this;
  }

  @Override
  public void paint(final Graphics g) {
    super.paint(g);
    g.setColor(Color.BLACK);
    float[] dash;
    if (this.dash == null) {
      dash = null;
    } else {
      dash = new float[this.dash.size()];

      for (int i = 0; i < dash.length; i++) {
        final float f = this.dash.get(i).floatValue();
        dash[i] = f;

      }
    }
    final Graphics2D g2 = (Graphics2D)g;
    g2.setStroke(new BasicStroke(1, 0, 0, 1, dash, 0));
    final int y = getHeight() / 2;
    g.drawLine(0, y, getWidth(), y);

  }
}
