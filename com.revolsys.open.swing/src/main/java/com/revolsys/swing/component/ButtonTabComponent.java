package com.revolsys.swing.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class ButtonTabComponent extends JPanel {
  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private class TabButton extends JButton implements ActionListener {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private final JTabbedPane tabs;

    public TabButton(final JTabbedPane tabs) {
      this.tabs = tabs;
      final int size = 17;
      setPreferredSize(new Dimension(size, size));
      setToolTipText("Close");
      setUI(new BasicButtonUI());
      setContentAreaFilled(false);
      setFocusable(false);
      setBorder(BorderFactory.createEtchedBorder());
      setBorderPainted(false);
      setRolloverEnabled(true);
      addActionListener(this);
    }

    @Override
    public void actionPerformed(final ActionEvent e) {
      final int i = this.tabs.indexOfTabComponent(ButtonTabComponent.this);
      if (i != -1) {
        this.tabs.remove(i);
      }
    }

    // paint the cross
    @Override
    protected void paintComponent(final Graphics g) {
      super.paintComponent(g);
      final Graphics2D g2 = (Graphics2D)g.create();
      // shift the image for pressed buttons
      final ButtonModel model = getModel();
      if (model.isPressed()) {
        g2.translate(1, 1);
      }
      g2.setStroke(new BasicStroke(2));
      g2.setColor(Color.BLACK);
      if (model.isRollover()) {
        g2.setColor(Color.MAGENTA);
      }
      final int delta = 5;
      final int width = getWidth();
      final int height = getHeight();
      g2.drawLine(delta, delta, width - delta - 1, height - delta - 1);
      g2.drawLine(width - delta - 1, delta, delta, height - delta - 1);
      g2.dispose();
    }

    @Override
    public void updateUI() {
    }
  }

  public ButtonTabComponent(final JTabbedPane tabs) {
    super(new FlowLayout(FlowLayout.LEFT, 0, 0));
    setOpaque(false);

    // make JLabel read titles from JTabbedPane
    final JLabel label = new JLabel() {
      /**
       *
       */
      private static final long serialVersionUID = 1L;

      @Override
      public String getText() {
        final int i = tabs.indexOfTabComponent(ButtonTabComponent.this);
        if (i != -1) {
          return tabs.getTitleAt(i);
        }
        return null;
      }
    };

    add(label);
    final JButton button = new TabButton(tabs);
    add(button);
  }
}
