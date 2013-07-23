package com.revolsys.swing.field;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.list.BaseListModel;
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.CollectionUtil;

public class StringListField extends ValueField {
  public static final String SELECTED = "selected";

  private static final long serialVersionUID = 1L;

  private final JTextField valueEntry = new JTextField();

  private final BaseListModel<String> values = new BaseListModel<String>();

  private final JButton addButton;

  private final Comparator<String> comparator;

  private final JXList valuesField;

  private final ToolBar toolBar = new ToolBar();

  public StringListField(final Comparator<String> comparator,
    final String fieldName) {
    super(fieldName, "");
    this.comparator = comparator;

    setLayout(new HorizontalLayout(2));

    final JPanel fieldPanel = new JPanel(new VerticalLayout(2));
    add(fieldPanel);

    toolBar.setOrientation(ToolBar.VERTICAL);
    add(toolBar);

    valueEntry.setPreferredSize(new Dimension(600, 25));
    fieldPanel.add(valueEntry);

    addButton = toolBar.addButtonTitleIcon("add", "Add", "add", this,
      "addValue");

    valueEntry.addActionListener(addButton.getAction());

    toolBar.addButtonTitleIcon(SELECTED, "Remove Selected", "delete", this,
      "removeSelectedValues");

    valuesField = new JXList(values);
    valuesField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    valuesField.setHighlighters(HighlighterFactory.createSimpleStriping(Color.LIGHT_GRAY));

    final JScrollPane namesPane = new JScrollPane(valuesField);
    namesPane.setPreferredSize(new Dimension(100, 3 * 20));
    fieldPanel.add(namesPane);
    updateFields();

    valueEntry.getDocument().addDocumentListener(
      new InvokeMethodListener(this, "updateFields"));

    valuesField.addListSelectionListener(new InvokeMethodListener(this,
      "updateFields"));

  }

  public StringListField(final String fieldName) {
    this(null, fieldName);
  }

  public void addValue() {
    final String value = valueEntry.getText();
    if (addValue(value)) {
      valueEntry.setText("");
    }
  }

  public boolean addValue(final String value) {
    if (StringUtils.hasText(value)) {
      if (!values.contains(value)) {

        if (comparator == null || values.isEmpty()) {
          values.add(value);
          valuesField.setSelectedIndex(values.size() - 1);
        } else {
          boolean inserted = false;
          for (int i = 0; i < values.size() && !inserted; i++) {
            final String listValue = values.get(i);
            if (comparator.compare(value, listValue) < 0) {
              values.add(i, value);
              inserted = true;
              valuesField.setSelectedIndex(i);
            }
          }
          if (!inserted) {
            values.add(value);
            valuesField.setSelectedIndex(values.size() - 1);
          }
        }
        updateFields();
        return true;
      }
    }
    valueEntry.requestFocusInWindow();
    return false;
  }

  public String getSelected() {
    return (String)valuesField.getSelectedValue();
  }

  public ToolBar getToolBar() {
    return toolBar;
  }

  public void removeSelectedValues() {
    final int[] selectedRows = valuesField.getSelectedIndices();
    if (selectedRows.length > 0) {
      for (final int selectedRow : selectedRows) {
        values.remove(selectedRow);
      }
      final int index = Math.min(selectedRows[0], values.size() - 1);
      if (index >= -1) {
        final ListSelectionModel selectionModel = valuesField.getSelectionModel();
        selectionModel.setSelectionInterval(index, index);
      }
    }
    updateFields();
    valueEntry.requestFocusInWindow();
  }

  @Override
  public void setFieldValue(final Object value) {
    if (!EqualsRegistry.equal(value, getFieldValue())) {
      if (values != null) {
        if (value == null) {
          values.clear();
        } else {
          final String string = value.toString();
          if (StringUtils.hasText(string)) {
            final List<String> newValues = new ArrayList<String>();
            for (final String item : string.replaceAll("\\s+", "").split(",+")) {
              if (StringUtils.hasText(item)) {
                newValues.add(item);
              }
            }
            if (comparator != null) {
              Collections.sort(newValues, comparator);
            }
            if (!EqualsRegistry.equal(values, newValues)) {
              values.clear();
              values.addAll(newValues);
              if (!newValues.isEmpty()) {
                valuesField.setSelectedIndex(0);
              }
            }
          }
        }
        super.setFieldValue(CollectionUtil.toString(values));
      }
    }
  }

  private void setSelectedButtonsEnabled(final boolean enabled) {
    for (final Component component : toolBar.getGroup(SELECTED)) {
      component.setEnabled(enabled);
    }
  }

  public void updateFields() {
    valueEntry.setEnabled(true);
    if (StringUtils.hasText(valueEntry.getText())) {
      addButton.setEnabled(true);
    } else {
      addButton.setEnabled(false);
    }
    if (values.size() == 0) {
      setSelectedButtonsEnabled(false);
    } else {
      setSelectedButtonsEnabled(true);
    }
  }
}
