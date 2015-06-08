package com.revolsys.swing.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.field.Field;

public class PreferencesDialog extends JDialog {
  private static final long serialVersionUID = 1L;

  private static final PreferencesDialog INSTANCE = new PreferencesDialog();

  public static PreferencesDialog get() {
    return INSTANCE;
  }

  private final JTabbedPane tabs = new JTabbedPane();

  private final Map<String, PreferencesPanel> panels = new HashMap<String, PreferencesPanel>();

  public PreferencesDialog() {
    super(null, "Preferences", ModalityType.APPLICATION_MODAL);
    add(this.tabs, BorderLayout.CENTER);

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonsPanel.add(InvokeMethodAction.createButton("Cancel", this, "cancel"));
    buttonsPanel.add(InvokeMethodAction.createButton("Save", this, "save"));
    add(buttonsPanel, BorderLayout.SOUTH);
  }

  public void addPanel(final PreferencesPanel panel) {
    if (!this.panels.containsValue(panel)) {
      final String title = panel.getTitle();
      this.panels.put(title, panel);

      final Component component = panel.getComponent();
      this.tabs.addTab(title, new JScrollPane(component));
    }
  }

  public void addPreference(final String title, final String applicationName, final String path,
    final String propertyName, final Class<?> valueClass, final Object defaultValue) {
    addPreference(title, applicationName, path, propertyName, valueClass, defaultValue, null);
  }

  public void addPreference(final String title, final String applicationName, final String path,
    final String propertyName, final Class<?> valueClass, final Object defaultValue,
    final Field field) {
    PreferencesPanel panel = this.panels.get(title);
    if (panel == null) {
      panel = new SimplePreferencesPanel(title);
      addPanel(panel);
    }
    if (panel instanceof SimplePreferencesPanel) {
      final SimplePreferencesPanel simplePanel = (SimplePreferencesPanel)panel;
      simplePanel.addPreference(applicationName, path, propertyName, valueClass, defaultValue,
        field);
    }
  }

  public void cancel() {
    for (final PreferencesPanel panel : this.panels.values()) {
      panel.cancelChanges();
    }
    setVisible(false);
  }

  public void save() {
    for (final PreferencesPanel panel : this.panels.values()) {
      panel.savePreferences();
    }
    setVisible(false);
  }

  public void showPanel() {
    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
    setMinimumSize(new Dimension(400, 300));
    pack();
    setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
    setVisible(true);
  }
}
