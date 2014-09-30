package com.revolsys.swing.map.form;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultRowSorter;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.dnd.transferhandler.ListReorderableTransferHandler;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.SearchField;
import com.revolsys.swing.list.BaseListModel;
import com.revolsys.swing.list.filter.StringContainsRowFilter;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class FieldNamesSetPanel extends ValueField implements ActionListener,
  ListSelectionListener, PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  private final JButton addButton;

  private final JXList allFieldNames;

  private final BaseListModel<String> allFieldNamesModel;

  private final List<String> fieldNamesSetNames;

  private final Map<String, List<String>> fieldNamesSets;

  private final JPanel fieldsPanel;

  private final JPanel filterPanel;

  private final AbstractRecordLayer layer;

  private final StringContainsRowFilter allFieldNamesTextFilter;

  private final JButton removeButton;

  private final JXList selectedFieldNames;

  private final BaseListModel<String> selectedFieldNamesModel;

  private final JButton moveUpButton;

  private final JButton moveDownButton;

  private final DefaultComboBoxModel<String> fieldNamesSetNamesModel;

  private final JButton renameButton;

  private final JButton deleteButton;

  public FieldNamesSetPanel(final AbstractRecordLayer layer) {
    super(new VerticalLayout(5));
    this.layer = layer;
    this.fieldNamesSetNames = layer.getFieldNamesSetNames();
    this.fieldNamesSets = new HashMap<>(layer.getFieldNamesSets());
    SwingUtil.setTitledBorder(this, "Field Sets");

    final List<String> fieldNamesSetNames = this.layer.getFieldNamesSetNames();
    this.fieldNamesSetNamesModel = ComboBox.model(fieldNamesSetNames);
    final ComboBox fieldNameSetNamesField = new ComboBox("fieldNamesSetName",
      this.fieldNamesSetNamesModel);
    int maxLength = 3;
    for (final String name : fieldNamesSetNames) {
      maxLength = Math.max(maxLength, name.length());
    }
    fieldNameSetNamesField.setMaximumSize(new Dimension(Math.max(300,
      maxLength * 11 + 40), 22));
    fieldNameSetNamesField.addPropertyChangeListener("fieldNamesSetName", this);

    final ToolBar toolBar = new ToolBar();
    toolBar.setOpaque(false);
    toolBar.addComponent("default", fieldNameSetNamesField);
    this.renameButton = toolBar.addButtonTitleIcon("default",
      "Rename Field Set", "fields_filter_edit", this, "actionRename");
    this.deleteButton = toolBar.addButtonTitleIcon("default",
      "Delete Field Set", "fields_filter_delete", this, "actionDelete");
    toolBar.addButtonTitleIcon("default", "Add Field Set", "fields_filter_add",
      this, "actionAdd");

    add(toolBar);

    this.filterPanel = new JPanel(new HorizontalLayout(46));
    this.filterPanel.setOpaque(false);
    add(this.filterPanel);

    final SearchField allFieldNamesFilterField = new SearchField(
      "allFieldNamesFilter");
    allFieldNamesFilterField.setPreferredSize(new Dimension(350, 25));
    allFieldNamesFilterField.addActionListener(this);
    this.filterPanel.add(allFieldNamesFilterField);

    this.fieldsPanel = new JPanel(new HorizontalLayout(5));
    this.fieldsPanel.setOpaque(false);

    this.allFieldNamesModel = new BaseListModel<>(layer.getFieldNames());
    this.allFieldNames = new JXList(this.allFieldNamesModel);
    this.allFieldNames.setAutoCreateRowSorter(true);
    this.allFieldNames.setSortable(true);
    this.allFieldNames.setSortOrder(SortOrder.ASCENDING);
    this.allFieldNames.addListSelectionListener(this);
    final JScrollPane layerPathsScrollPane = new JScrollPane(this.allFieldNames);
    layerPathsScrollPane.setPreferredSize(new Dimension(350, 400));
    this.fieldsPanel.add(layerPathsScrollPane);

    final ToolBar fieldsToolBar = new ToolBar(ToolBar.VERTICAL);
    fieldsToolBar.setOpaque(false);
    fieldsToolBar.setMinimumSize(new Dimension(25, 25));
    this.fieldsPanel.add(fieldsToolBar);

    this.addButton = fieldsToolBar.addButtonTitleIcon("default", "Add", "add",
      this, "actionAddSelected");
    this.removeButton = fieldsToolBar.addButtonTitleIcon("default", "Remove",
      "delete", this, "actionRemoveSelected");

    this.moveUpButton = fieldsToolBar.addButtonTitleIcon("default", "Move Up",
      "arrow_up", this, "actionMoveSelectedUp");
    this.moveDownButton = fieldsToolBar.addButtonTitleIcon("default",
      "Move Down", "arrow_down", this, "actionMoveSelectedDown");

    this.selectedFieldNamesModel = new BaseListModel<String>();

    this.selectedFieldNames = new JXList(this.selectedFieldNamesModel);
    this.selectedFieldNames.setAutoCreateRowSorter(false);
    this.selectedFieldNames.setSortable(false);
    this.selectedFieldNames.addListSelectionListener(this);
    this.selectedFieldNames.setDragEnabled(true);
    this.selectedFieldNames.setDropMode(DropMode.INSERT);
    this.selectedFieldNames.setTransferHandler(new ListReorderableTransferHandler(
      this.selectedFieldNames));

    final JScrollPane snapScrollPane = new JScrollPane(this.selectedFieldNames);
    snapScrollPane.setPreferredSize(new Dimension(350, 400));
    this.fieldsPanel.add(snapScrollPane);
    add(this.fieldsPanel);

    this.allFieldNamesTextFilter = new StringContainsRowFilter();
    final RowFilter<ListModel<?>, Integer> allFieldNamesFilter = RowFilter.andFilter(Arrays.asList(
      new CollectionRowFilter(this.selectedFieldNamesModel, false),
      this.allFieldNamesTextFilter));
    this.allFieldNames.setRowFilter(allFieldNamesFilter);

    setFieldNamesSetName("All");
    updateEnabledState();
  }

  public void actionAdd() {
    final String name = JOptionPane.showInputDialog(
      SwingUtil.getActiveWindow(), "Enter the name of the new field set.",
      "Add Field Set", JOptionPane.PLAIN_MESSAGE);
    if (Property.hasValue(name)) {
      boolean found = false;
      for (int i = 0; i < this.fieldNamesSetNames.size(); i++) {
        final String name2 = this.fieldNamesSetNames.get(i);
        if (name2.equalsIgnoreCase(name)) {
          this.fieldNamesSetNames.set(i, name);
          final List<String> names = this.fieldNamesSets.remove(name2);
          this.fieldNamesSets.put(name, names);
          found = true;
        }
      }
      if (!found) {
        this.fieldNamesSetNames.add(name);
        this.fieldNamesSetNamesModel.addElement(name);
        this.fieldNamesSetNamesModel.setSelectedItem(name);
      }
    }
  }

  public void actionAddSelected() {
    this.selectedFieldNames.clearSelection();
    for (final Object selectedValue : this.allFieldNames.getSelectedValues()) {
      final String fieldName = (String)selectedValue;
      if (!this.selectedFieldNamesModel.contains(fieldName)) {
        this.selectedFieldNamesModel.add(fieldName);
        final int index = this.selectedFieldNames.convertIndexToView(this.selectedFieldNamesModel.indexOf(fieldName));
        this.selectedFieldNames.addSelectionInterval(index, index);
      }
      this.allFieldNamesModel.remove(fieldName);
    }
    updateEnabledState();
  }

  public void actionDelete() {
    final String fieldSetName = (String)this.fieldNamesSetNamesModel.getSelectedItem();
    if ("All".equalsIgnoreCase(fieldSetName)) {
      Toolkit.getDefaultToolkit().beep();
    } else {
      final int result = JOptionPane.showConfirmDialog(
        SwingUtil.getActiveWindow(), "Delete field set " + fieldSetName + ".",
        "Delete Field Set", JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.OK_OPTION) {
        for (int i = 0; i < this.fieldNamesSetNames.size(); i++) {
          final String name2 = this.fieldNamesSetNames.get(i);
          if (fieldSetName.equalsIgnoreCase(name2)) {
            this.fieldNamesSetNames.remove(i);
            this.fieldNamesSets.remove(name2);
            this.fieldNamesSetNamesModel.removeElement(name2);
          }
        }
      }
    }
  }

  public void actionMoveSelectedDown() {
    final int selectedIndex = this.selectedFieldNames.getSelectedIndex();
    if (selectedIndex < this.selectedFieldNamesModel.getSize() - 1) {
      final int newStartIndex = selectedIndex + 1;
      int newIndex = newStartIndex;
      final Object[] selectedValues = this.selectedFieldNames.getSelectedValues();
      this.selectedFieldNamesModel.removeAll(selectedValues);
      for (final Object selectedValue : selectedValues) {
        final String fieldName = (String)selectedValue;
        this.selectedFieldNamesModel.add(newIndex++, fieldName);
      }
      final ListSelectionModel selectionModel = this.selectedFieldNames.getSelectionModel();
      selectionModel.setSelectionInterval(newStartIndex, newIndex - 1);
    }
    updateEnabledState();
  }

  public void actionMoveSelectedUp() {
    final int selectedIndex = this.selectedFieldNames.getSelectedIndex();
    if (selectedIndex > 0) {
      final int newStartIndex = selectedIndex - 1;
      int newIndex = newStartIndex;
      final Object[] selectedValues = this.selectedFieldNames.getSelectedValues();
      this.selectedFieldNamesModel.removeAll(selectedValues);
      for (final Object selectedValue : selectedValues) {
        final String fieldName = (String)selectedValue;
        this.selectedFieldNamesModel.add(newIndex++, fieldName);
      }
      final ListSelectionModel selectionModel = this.selectedFieldNames.getSelectionModel();
      selectionModel.setSelectionInterval(newStartIndex, newIndex - 1);
    }
    updateEnabledState();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final Object source = event.getSource();
    if (source instanceof SearchField) {
      final SearchField field = (SearchField)source;
      final String fieldName = field.getFieldName();
      final String text = field.getText();
      if (fieldName.equals("allFieldNamesFilter")) {
        this.allFieldNamesTextFilter.setFilterText(text);
        sort(this.allFieldNames);
      }
    }
  }

  public void actionRemoveSelected() {
    final Object[] selectedValues = this.selectedFieldNames.getSelectedValues();
    this.selectedFieldNamesModel.removeAll(selectedValues);
    for (final Object selectedValue : selectedValues) {
      final String fieldName = (String)selectedValue;
      this.allFieldNamesModel.add(fieldName);
    }
    sort(this.allFieldNames);
    updateEnabledState();
  }

  public void actionRename() {
    final String fieldSetName = (String)this.fieldNamesSetNamesModel.getSelectedItem();
    if ("All".equalsIgnoreCase(fieldSetName)) {
      Toolkit.getDefaultToolkit().beep();
    } else {
      final String name = (String)JOptionPane.showInputDialog(
        SwingUtil.getActiveWindow(), "Enter the new name for the field set.",
        "Rename Field Set", JOptionPane.PLAIN_MESSAGE, null, null, fieldSetName);
      if (Property.hasValue(name)) {
        for (int i = 0; i < this.fieldNamesSetNames.size(); i++) {
          final String name2 = this.fieldNamesSetNames.get(i);
          if (fieldSetName.equalsIgnoreCase(name2)) {
            this.fieldNamesSetNames.set(i, name);
            this.fieldNamesSets.put(name, new ArrayList<>(
              this.selectedFieldNamesModel));
            this.fieldNamesSetNamesModel.removeElement(name2);
            this.fieldNamesSetNamesModel.insertElementAt(name, i);
            this.fieldNamesSetNamesModel.setSelectedItem(name);
          } else if (name2.equalsIgnoreCase(name)) {
            JOptionPane.showMessageDialog(SwingUtil.getActiveWindow(),
              "New name already in use: " + name2, "Rename Field Set",
              JOptionPane.ERROR_MESSAGE);
            return;
          }
        }
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if (propertyName.equals("fieldNamesSetName")) {
      final String oldFieldNamesSetName = (String)event.getOldValue();
      if (oldFieldNamesSetName != null) {
        this.fieldNamesSets.put(oldFieldNamesSetName, new ArrayList<>(
          this.selectedFieldNamesModel));
      }
      final String newFieldNamesSetName = (String)event.getNewValue();
      setFieldNamesSetName(newFieldNamesSetName);
      updateEnabledState();
    }
  }

  @Override
  public void save() {
    super.save();
    final List<String> namesToSave = new ArrayList<>();
    for (int i = 0; i < this.selectedFieldNamesModel.size(); i++) {
      final String fieldName = this.selectedFieldNamesModel.get(i);
      namesToSave.add(fieldName);
    }
    final String fieldSetName = (String)this.fieldNamesSetNamesModel.getSelectedItem();
    this.fieldNamesSets.put(fieldSetName, namesToSave);

    final Map<String, List<String>> fieldNamesSets = new LinkedHashMap<>();
    for (final String fieldNamesSetName : this.fieldNamesSetNames) {
      final List<String> fieldNames = this.fieldNamesSets.get(fieldNamesSetName);
      if (Property.hasValue(fieldNames)) {
        fieldNamesSets.put(fieldNamesSetName, fieldNames);
      }
    }
    this.layer.setFieldNamesSets(fieldNamesSets);
  }

  public void setFieldNamesSetName(final String fieldNamesSetName) {
    final List<String> allFieldNames = this.layer.getFieldNames();
    List<String> selectedFieldNames = this.fieldNamesSets.get(fieldNamesSetName);
    if (selectedFieldNames == null) {
      selectedFieldNames = new ArrayList<>();
    }
    allFieldNames.removeAll(selectedFieldNames);
    this.allFieldNamesModel.setAll(allFieldNames);
    this.allFieldNamesTextFilter.setFilterText("");
    this.allFieldNames.setSelectedIndex(0);
    this.allFieldNames.setRowFilter(this.allFieldNamesTextFilter);
    this.selectedFieldNamesModel.setAll(selectedFieldNames);
    this.selectedFieldNames.setSelectedIndex(0);
  }

  @SuppressWarnings("rawtypes")
  public void sort(final JXList list) {
    final RowSorter<? extends ListModel> rowSorter = list.getRowSorter();
    if (rowSorter instanceof DefaultRowSorter) {
      final DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>)rowSorter;
      sorter.sort();
    }
  }

  public void updateEnabledState() {
    final String fieldSetName = (String)this.fieldNamesSetNamesModel.getSelectedItem();
    final boolean editEnabled = !"All".equalsIgnoreCase(fieldSetName);
    this.deleteButton.setEnabled(editEnabled);
    this.renameButton.setEnabled(editEnabled);

    this.addButton.setEnabled(this.allFieldNames.getSelectedIndex() > -1);
    final int selectedFieldIndex = this.selectedFieldNames.getSelectedIndex();
    this.removeButton.setEnabled(selectedFieldIndex > -1);
    this.moveUpButton.setEnabled(selectedFieldIndex > 0);
    this.moveDownButton.setEnabled(selectedFieldIndex > -1
      && selectedFieldIndex < this.selectedFieldNamesModel.getSize() - 1);
  }

  @Override
  public void valueChanged(final ListSelectionEvent event) {
    updateEnabledState();
  }
}
