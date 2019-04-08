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
import javax.swing.SwingConstants;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.VerticalLayout;
import org.jdesktop.swingx.decorator.HighlighterFactory;
import org.jeometry.common.data.type.DataType;

import com.revolsys.swing.EventQueue;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.list.ArrayListModel;
import com.revolsys.swing.listener.EventQueueRunnableListener;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;
import com.revolsys.util.Strings;

public class StringListField extends ValueField {
  public static final String SELECTED = "selected";

  private static final long serialVersionUID = 1L;

  private final JButton addButton;

  private final Comparator<String> comparator;

  private final ToolBar toolBar = new ToolBar();

  private final JTextField valueEntry = new JTextField();

  private final ArrayListModel<String> values = new ArrayListModel<>();

  private final JXList valuesField;

  public StringListField(final Comparator<String> comparator, final String fieldName) {
    super(new HorizontalLayout(2), fieldName, "");
    setOpaque(false);
    this.comparator = comparator;

    final JPanel fieldPanel = new JPanel(new VerticalLayout(2));
    fieldPanel.setOpaque(false);
    add(fieldPanel);

    this.toolBar.setOpaque(false);
    this.toolBar.setOrientation(SwingConstants.VERTICAL);
    add(this.toolBar);

    this.valueEntry.setPreferredSize(new Dimension(600, 25));
    fieldPanel.add(this.valueEntry);

    this.addButton = this.toolBar.addButtonTitleIcon("add", "Add", "add", this::addValue);

    this.valueEntry.addActionListener(this.addButton.getAction());

    this.toolBar.addButtonTitleIcon(SELECTED, "Remove Selected", "delete",
      this::removeSelectedValues);

    this.valuesField = new JXList(this.values);
    this.valuesField.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.valuesField.setHighlighters(HighlighterFactory.createSimpleStriping(Color.LIGHT_GRAY));

    final JScrollPane namesPane = new JScrollPane(this.valuesField);
    namesPane.setPreferredSize(new Dimension(100, 3 * 20));
    fieldPanel.add(namesPane);
    updateFields();

    final EventQueueRunnableListener updateFieldListener = EventQueue.addDocument(this.valueEntry,
      () -> updateFields());

    this.valuesField.addListSelectionListener(updateFieldListener);

  }

  public StringListField(final String fieldName) {
    this(null, fieldName);
  }

  public void addValue() {
    final String value = this.valueEntry.getText();
    if (addValue(value)) {
      this.valueEntry.setText("");
    }
  }

  public boolean addValue(final String value) {
    if (Property.hasValue(value)) {
      if (!this.values.contains(value)) {

        if (this.comparator == null || this.values.isEmpty()) {
          this.values.add(value);
          this.valuesField.setSelectedIndex(this.values.size() - 1);
        } else {
          boolean inserted = false;
          for (int i = 0; i < this.values.size() && !inserted; i++) {
            final String listValue = this.values.get(i);
            if (this.comparator.compare(value, listValue) < 0) {
              this.values.add(i, value);
              inserted = true;
              this.valuesField.setSelectedIndex(i);
            }
          }
          if (!inserted) {
            this.values.add(value);
            this.valuesField.setSelectedIndex(this.values.size() - 1);
          }
        }
        updateFields();
        setFieldValue(Strings.toString(this.values));
        return true;
      }
    }
    this.valueEntry.requestFocusInWindow();
    return false;
  }

  public String getSelected() {
    return (String)this.valuesField.getSelectedValue();
  }

  public ToolBar getToolBar() {
    return this.toolBar;
  }

  public void removeSelectedValues() {
    final int[] selectedRows = this.valuesField.getSelectedIndices();
    if (selectedRows.length > 0) {
      for (final int selectedRow : selectedRows) {
        this.values.remove(selectedRow);
      }
      final int index = Math.min(selectedRows[0], this.values.size() - 1);
      if (index >= -1) {
        final ListSelectionModel selectionModel = this.valuesField.getSelectionModel();
        selectionModel.setSelectionInterval(index, index);
      }
    }
    setFieldValue(Strings.toString(this.values));
    updateFields();
    this.valueEntry.requestFocusInWindow();
  }

  @Override
  public boolean setFieldValue(final Object value) {
    if (!DataType.equal(value, getFieldValue())) {
      if (this.values != null) {
        if (value == null) {
          this.values.clear();
        } else {
          final String string = value.toString();
          if (Property.hasValue(string)) {
            final List<String> newValues = new ArrayList<>();
            for (final String item : string.replaceAll("\\s+", "").split(",+")) {
              if (Property.hasValue(item)) {
                newValues.add(item);
              }
            }
            if (this.comparator != null) {
              Collections.sort(newValues, this.comparator);
            }
            if (!DataType.equal(this.values, newValues)) {
              this.values.clear();
              this.values.addAll(newValues);
              if (!newValues.isEmpty()) {
                this.valuesField.setSelectedIndex(0);
              }
            }
          }
        }
        return super.setFieldValue(Strings.toString(this.values));
      }
    }
    return false;
  }

  private void setSelectedButtonsEnabled(final boolean editable) {
    for (final Component component : this.toolBar.getGroup(SELECTED)) {
      component.setEnabled(editable);
      // this.valueEntry.setEditable(editable);
    }
  }

  public void updateFields() {
    this.valueEntry.setEnabled(true);
    final String text = this.valueEntry.getText();
    if (Property.hasValue(text) && !this.values.contains(text)) {
      this.addButton.setEnabled(true);
    } else {
      this.addButton.setEnabled(false);
    }
    if (this.values.size() == 0) {
      setSelectedButtonsEnabled(false);
    } else {
      setSelectedButtonsEnabled(true);
    }
  }
}
