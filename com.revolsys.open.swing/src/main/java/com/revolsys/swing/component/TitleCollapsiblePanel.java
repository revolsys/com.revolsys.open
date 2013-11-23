package com.revolsys.swing.component;

import java.awt.Component;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.SwingUtilities;

import org.jdesktop.swingx.JXCollapsiblePane;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.SwingUtil;

public class TitleCollapsiblePanel extends BasePanel implements MouseListener {

  private final JXCollapsiblePane collapsible;

  private final ComponentFactory<?> componentFactory;

  private boolean created = false;

  public TitleCollapsiblePanel(final String title, final Component component) {
    this(title, null, component);
  }

  public TitleCollapsiblePanel(final String title,
    final ComponentFactory<?> componentFactory) {
    this(title, componentFactory, null);
  }

  private TitleCollapsiblePanel(final String title,
    final ComponentFactory<?> componentFactory, final Component component) {
    super(new VerticalLayout());
    this.componentFactory = componentFactory;

    SwingUtil.setTitledBorder(this, title);
    collapsible = new JXCollapsiblePane();
    collapsible.setCollapsed(true);
    collapsible.setOpaque(false);
    ((JXPanel)collapsible.getContentPane()).setOpaque(false);
    add(collapsible);

    if (component != null) {
      collapsible.add(component);
      created = true;
    }
    addMouseListener(this);
  }

  public boolean isCollapsed() {
    return collapsible.isCollapsed();
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
        if (!created) {
          created = true;
          if (componentFactory != null) {
            final Component component = componentFactory.createComponent();
            collapsible.add(component);
          }
        }
      }
    }
    collapsible.setCollapsed(collapsed);
  }
}
