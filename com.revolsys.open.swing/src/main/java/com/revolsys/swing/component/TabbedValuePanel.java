package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

import org.springframework.util.StringUtils;

public class TabbedValuePanel extends ValueField {
  private static final long serialVersionUID = 1L;

  private final JTabbedPane tabs = new JTabbedPane();

  public TabbedValuePanel() {
    super(new BorderLayout());
    add(tabs, BorderLayout.CENTER);
  }

  public TabbedValuePanel(final Object value) {
    this("Edit " + value, value);
  }

  public TabbedValuePanel(final String title, final Object value) {
    this();
    setTitle(title);
    setFieldValue(value);
  }

  public void addTab(final String title, final Component component) {
    tabs.addTab(title, component);
  }

  public void addTab(final ValueField panel) {
    final String title = panel.getTitle();
    tabs.addTab(title, panel);
  }

  public JTabbedPane getTabs() {
    return tabs;
  }

  public void setSelectdTab(final String tabName) {
    if (StringUtils.hasText(tabName)) {
      for (int i = 0; i < tabs.getTabCount(); i++) {
        final String name = tabs.getTitleAt(i);
        if (tabName.equals(name)) {
          tabs.setSelectedIndex(i);
        }
      }
    }
  }
}
