package com.revolsys.swing.field;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.springframework.util.StringUtils;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.swing.action.InvokeMethodAction;
import com.revolsys.swing.component.ValueField;
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

  private final JList valuesField;

  public StringListField(final String fieldName) {
    super(fieldName, "");

    setLayout(new BorderLayout());

    final JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

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
    add(buttonsPanel, BorderLayout.NORTH);

    valuesField = new JList(values);

    final JScrollPane namesPane = new JScrollPane(valuesField);
    namesPane.setPreferredSize(new Dimension(100, 3 * 20));
    add(namesPane, BorderLayout.SOUTH);
    updateFields();

    valueEntry.getDocument().addDocumentListener(
      new InvokeMethodListener(this, "updateFields"));

    valuesField.addListSelectionListener(new EnableComponentListener(
      removeButton));

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
        values.add(value);
        updateFields();
        return true;
      }
    }
    return false;
  }

  public void clear() {
    values.clear();
    updateFields();
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
  }

  @Override
  public void setFieldValue(final Object value) {
    if (!EqualsRegistry.equal(value, getFieldValue())) {
      super.setFieldValue(value);
      if (values != null) {
        values.clear();
        if (value != null) {
          values.addAll(Arrays.asList(value.toString()
            .replaceAll("\\s+", "")
            .split(",+")));
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
