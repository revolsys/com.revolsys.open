package com.revolsys.swing;

import java.awt.Component;

import javax.swing.Icon;
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
}
