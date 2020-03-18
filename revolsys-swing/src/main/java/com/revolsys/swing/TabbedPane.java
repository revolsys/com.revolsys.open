package com.revolsys.swing;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import com.revolsys.swing.component.TabClosableTitle;

public class TabbedPane extends JTabbedPane {
  private static final long serialVersionUID = 1L;

  public TabbedPane() {
    super();
  }

  public TabbedPane(final int tabPlacement) {
    super(tabPlacement);
  }

  public TabbedPane(final int tabPlacement, final int tabLayoutPolicy) {
    super(tabPlacement, tabLayoutPolicy);
  }

  public TabClosableTitle addClosableTab(final String title, final Icon icon,
    final Component component, final Runnable closeAction) {
    final int tabIndex = getTabCount();
    addTab(title, icon, component);

    final TabClosableTitle tabTitle = new TabClosableTitle(this, closeAction);
    setTabComponentAt(tabIndex, tabTitle);
    return tabTitle;
  }

  public int addTab(final Icon icon, final String toolTipText, final Component component) {
    return addTab(icon, toolTipText, component, false);
  }

  public int addTab(final Icon icon, final String toolTipText, Component component,
    final boolean useScrollPane) {
    if (useScrollPane) {
      final JScrollPane scrollPane = new JScrollPane(component);
      scrollPane.setBorder(BorderFactory.createEmptyBorder());
      component = scrollPane;
    }

    addTab(null, icon, component);
    final int tabIndex = getTabCount() - 1;
    setToolTipTextAt(tabIndex, toolTipText);
    return tabIndex;
  }

  public int addTabIcon(final String iconName, final String toolTipText, final Component component,
    final boolean useScrollPane) {
    final Icon icon = Icons.getIcon(iconName);
    return addTab(icon, toolTipText, component, useScrollPane);
  }

  public int getTabIndexByTitle(final String title) {
    final int tabCount = getTabCount();
    for (int i = 0; i < tabCount; i++) {
      final String tabTitle = getTitleAt(i);
      if (tabTitle.equals(title)) {
        return i;
      }
    }
    return -1;
  }
}
