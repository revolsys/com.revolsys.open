package com.revolsys.swing.preferences;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.layout.GroupLayouts;

public abstract class AbstractPreferencesPanel extends JPanel implements PreferencesPanel {
  private static final long serialVersionUID = 1L;

  private final JPanel fieldPanel = new JPanel();

  private final String title;

  public AbstractPreferencesPanel(final String title, final String instructions) {
    super(new BorderLayout());
    setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
    this.title = title;
    if (instructions != null) {
      final JLabel instructionsLabel = new JLabel(
        "<html><p style=\"color:#666666\">" + instructions + "</p></html>");
      add(instructionsLabel, BorderLayout.NORTH);
    }
    add(this.fieldPanel, BorderLayout.CENTER);
  }

  public void addField(final Field field) {
    addField(field.getFieldName(), (Component)field);
  }

  public void addField(final String title, final Component component) {
    SwingUtil.addLabel(this.fieldPanel, title);
    this.fieldPanel.add(component);
    GroupLayouts.makeColumns(this.fieldPanel, 2, true);
  }

  @Override
  public void cancelChanges() {
  }

  protected void doSavePreferences() {
  }

  @Override
  public Component getComponent() {
    return this;
  }

  @Override
  public String getTitle() {
    return this.title;
  }

  @Override
  public boolean isPreferencesValid() {
    return true;
  }

  @Override
  public boolean savePreferences() {
    if (isPreferencesValid()) {
      doSavePreferences();
      return true;
    } else {
      return false;
    }
  }
}
