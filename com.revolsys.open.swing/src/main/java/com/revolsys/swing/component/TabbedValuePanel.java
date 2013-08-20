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
    add(this.tabs, BorderLayout.CENTER);
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
    this.tabs.addTab(title, component);
  }

  public void addTab(final ValueField panel) {
    final String title = panel.getTitle();
    this.tabs.addTab(title, panel);
  }

  public JTabbedPane getTabs() {
    return this.tabs;
  }

  public void setSelectdTab(final String tabName) {
    if (StringUtils.hasText(tabName)) {
      for (int i = 0; i < this.tabs.getTabCount(); i++) {
        final String name = this.tabs.getTitleAt(i);
        if (tabName.equals(name)) {
          this.tabs.setSelectedIndex(i);
        }
      }
    }
  }
}
