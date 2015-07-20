package com.revolsys.swing.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;

import org.slf4j.LoggerFactory;

import com.revolsys.swing.SwingUtil;

/**
 * Component to be used as tabComponent;
 * Contains a JLabel to show the text and
 * a JButton to close the tab it belongs to
 */
public class TabCloseTitle extends JLabel implements MouseListener {
  private static final long serialVersionUID = 1L;

  private final JTabbedPane tabs;

  private final Runnable closeAction;

  public TabCloseTitle(final JTabbedPane tabs, final Runnable closeAction) {
    this.tabs = tabs;
    setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 1));
    setOpaque(false);
    addMouseListener(this);
    this.closeAction = closeAction;
  }

  @Override
  public Icon getIcon() {
    if (this.tabs != null) {
      final int i = this.tabs.indexOfTabComponent(this);
      if (i != -1) {
        return this.tabs.getIconAt(i);
      }
    }
    return null;
  }

  @Override
  public Dimension getPreferredSize() {
    final Dimension preferredSize = super.getPreferredSize();
    preferredSize.width += 12;
    preferredSize.height = Math.max(12, preferredSize.height);
    return preferredSize;
  }

  @Override
  public String getText() {
    if (this.tabs != null) {
      final int i = this.tabs.indexOfTabComponent(this);
      if (i != -1) {
        final String title = this.tabs.getTitleAt(i);
        return title;
      }
    }
    return null;
  }

  protected boolean isInCloseButton(final MouseEvent e) {
    final int x1 = getWidth() - getInsets().right - 8;
    final int y1 = 0;
    final int x2 = getWidth();
    final int y2 = getHeight();
    final int x = e.getX();
    final int y = e.getY();
    final boolean inCloseButton = x1 <= x && x <= x2 && y1 <= y && y <= y2;
    return inCloseButton;
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
  }

  @Override
  public void mouseExited(final MouseEvent e) {
  }

  @Override
  public void mousePressed(final MouseEvent e) {
  }

  @Override
  public void mouseReleased(final MouseEvent event) {
    if (SwingUtil.isLeftButtonAndNoModifiers(event)) {
      final boolean inCloseButton = isInCloseButton(event);
      if (inCloseButton) {
        final String title = getText();
        final int i = this.tabs.indexOfTabComponent(this);
        if (i != -1) {
          this.tabs.remove(i);
        }
        if (this.closeAction != null) {
          try {
            this.closeAction.run();
          } catch (final Throwable e) {
            LoggerFactory.getLogger(getClass()).error("Unable to close tab: " + title, e);
          }
        }
      }
    }
  }

  @Override
  protected void paintComponent(final Graphics graphics) {
    super.paintComponent(graphics);
    final Graphics2D graphics2d = (Graphics2D)graphics;
    graphics2d.setStroke(new BasicStroke(2));
    graphics2d.setColor(Color.BLACK);
    final int x1 = getWidth() - getInsets().right - 9;
    final int y1 = getInsets().top + 4;
    final int x2 = x1 + 7;
    final int y2 = y1 + 7;
    graphics2d.drawLine(x1, y1, x2, y2);
    graphics2d.drawLine(x1, y2, x2, y1);
  }
}
