package com.revolsys.swing.component;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.Callable;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.logging.Logs;

import com.revolsys.swing.SwingUtil;

public class TitleCollapsiblePanel extends BasePanel implements MouseListener {
  private static final long serialVersionUID = 1L;

  private final JXCollapsiblePane collapsible;

  private final Callable<Component> componentFactory;

  private boolean created = false;

  public TitleCollapsiblePanel(final String title, final Callable<Component> componentFactory) {
    this(title, componentFactory, null);
  }

  private TitleCollapsiblePanel(final String title, final Callable<Component> componentFactory,
    final Component component) {
    super(new VerticalLayout());
    this.componentFactory = componentFactory;

    SwingUtil.setTitledBorder(this, title);
    this.collapsible = new JXCollapsiblePane();
    this.collapsible.setCollapsed(true);
    this.collapsible.setOpaque(false);
    ((JXPanel)this.collapsible.getContentPane()).setOpaque(false);
    add(this.collapsible);

    if (component != null) {
      this.collapsible.add(component);
      this.created = true;
    }
    addMouseListener(this);
  }

  public TitleCollapsiblePanel(final String title, final Component component) {
    this(title, null, component);
  }

  public boolean isCollapsed() {
    return this.collapsible.isCollapsed();
  }

  @Override
  public void mouseClicked(final MouseEvent e) {
    if (SwingUtilities.isLeftMouseButton(e)) {
      final Insets insets = getInsets();
      final int y = e.getY();
      if (y <= insets.top) {
        final boolean newCollapsed = !isCollapsed();
        setCollapsed(newCollapsed);
      }
    }
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
  public void mouseReleased(final MouseEvent e) {
  }

  public void setCollapsed(final boolean collapsed) {
    if (collapsed == false) {
      synchronized (this) {
        if (!this.created) {
          this.created = true;
          if (this.componentFactory != null) {
            try {
              final Component component = this.componentFactory.call();
              this.collapsible.add(component);
            } catch (final Exception e) {
              Logs.error(this, e);
            }
          }
        }
      }
    }
    this.collapsible.setCollapsed(collapsed);
  }
}
