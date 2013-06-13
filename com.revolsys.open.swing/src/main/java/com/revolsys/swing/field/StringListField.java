package com.revolsys.swing.field;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.jdesktop.swingx.VerticalLayout;
import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.list.BaseListModel;
import com.revolsys.swing.listener.EnableComponentListener;
import com.revolsys.swing.listener.InvokeMethodListener;
import com.revolsys.util.CollectionUtil;

public class StringListField extends ValueField {
  private static final long serialVersionUID = 1L;

  private final JTextField valueEntry = new JTextField();

  private final BaseListModel<String> values = new BaseListModel<String>();

  private final JButton addButton;

  private final JButton removeButton;

  private final Comparator<String> comparator;

  private final JList valuesField;

  public StringListField(final Comparator<String> comparator,
    final String fieldName) {
    super(fieldName, "");
    this.comparator = comparator;

    setLayout(new VerticalLayout(2));

    final JPanel buttonsPanel = new JPanel();

    valueEntry.setPreferredSize(new Dimension(600, 25));
    buttonsPanel.add(valueEntry);

    final InvokeMethodAction addAction = new InvokeMethodAction(null, "Add",
      SilkIconLoader.getIcon("add"), this, "addValue");
    addButton = new JButton(addAction);
    buttonsPanel.add(addButton);

    valueEntry.addActionListener(addAction);

    removeButton = new JButton(new InvokeMethodAction(null, "Remove Selected",
      SilkIconLoader.getIcon("delete"), this, "removeSelectedValues"));
    buttonsPanel.add(removeButton);
    add(buttonsPanel);

    GroupLayoutUtil.makeColumns(buttonsPanel, 3);
    valuesField = new JList(values);

    final JScrollPane namesPane = new JScrollPane(valuesField);
    namesPane.setPreferredSize(new Dimension(100, 3 * 20));
    add(namesPane);
    updateFields();

    valueEntry.getDocument().addDocumentListener(
      new InvokeMethodListener(this, "updateFields"));

    valuesField.addListSelectionListener(new EnableComponentListener(
      removeButton));

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
      super.setFieldValue(value);
      if (values != null) {
        values.clear();
        if (value != null) {
          final List<String> newValues = new ArrayList<String>(
            Arrays.asList(value.toString().replaceAll("\\s+", "").split(",+")));
          if (comparator != null) {
            Collections.sort(newValues, comparator);
          }
          values.addAll(newValues);
        }
      }
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
      removeButton.setEnabled(false);
    } else {
      final int[] selectedRows = valuesField.getSelectedIndices();
      if (selectedRows.length == 0) {
        valuesField.setSelectedIndex(0);
      }
      removeButton.setEnabled(true);
    }
    setFieldValue(CollectionUtil.toString(values));
  }
}
