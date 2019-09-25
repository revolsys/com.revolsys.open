package com.revolsys.swing.preferences;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.WindowConstants;

import com.revolsys.swing.action.RunnableAction;
import com.revolsys.swing.component.BaseDialog;

public class PreferencesDialog extends BaseDialog {

  private static final long serialVersionUID = 1L;

  private final Map<String, PreferencesPanel> panels = new HashMap<>();

  private final JTabbedPane tabs = new JTabbedPane();

  public PreferencesDialog() {
    super("Preferences", ModalityType.APPLICATION_MODAL);
    add(this.tabs, BorderLayout.CENTER);

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    buttonsPanel.add(RunnableAction.newButton("Cancel", this::cancel));
    buttonsPanel.add(RunnableAction.newButton("Save", this::save));
    add(buttonsPanel, BorderLayout.SOUTH);
    PreferenceFields.initDialog(this);
  }

  public void addPanel(final PreferencesPanel panel) {
    if (!this.panels.containsValue(panel)) {
      final String title = panel.getTitle();
      this.panels.put(title, panel);

      final Component component = panel.getComponent();
      this.tabs.addTab(title, new JScrollPane(component));
    }
  }

  public void cancel() {
    for (final PreferencesPanel panel : this.panels.values()) {
      panel.cancelChanges();
    }
    setVisible(false);
  }

  public PreferencesPanel getPanel(final String panelTitle) {
    return this.panels.get(panelTitle);
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
