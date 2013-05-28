package com.revolsys.swing.component;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.JTabbedPane;

import org.springframework.util.StringUtils;

public class TabbedValuePanel<T> extends ValueField<T> {
  private static final long serialVersionUID = 1L;

  private JTabbedPane tabs = new JTabbedPane();

  public TabbedValuePanel() {
    super(new BorderLayout());
    add(tabs, BorderLayout.CENTER);
  }

  public TabbedValuePanel(T value) {
    this("Edit " + value, value);
  }

  public TabbedValuePanel(String title, T value) {
    this();
    setTitle(title);
    setFieldValue(value);
  }

  public JTabbedPane getTabs() {
    return tabs;
  }

  public void addTab(String title, Component component) {
    tabs.addTab(title, component);
  }

  public void addTab(ValueField<?> panel) {
    String title = panel.getTitle();
    tabs.addTab(title, panel);
  }

  public void setSelectdTab(String tabName) {
    if (StringUtils.hasText(tabName)) {
      for (int i = 0; i < tabs.getTabCount(); i++) {
        String name = tabs.getTitleAt(i);
        if (tabName.equals(name)) {
          tabs.setSelectedIndex(i);
        }
      }
    }
  }
}
