package com.revolsys.swing.map.form;

import java.awt.Dimension;
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
import javax.swing.SwingConstants;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.Dialogs;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.dnd.transferhandler.ListReorderableTransferHandler;
import com.revolsys.swing.field.ArrayListComboBoxModel;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.SearchField;
import com.revolsys.swing.list.ArrayListModel;
import com.revolsys.swing.list.filter.StringContainsRowFilter;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.toolbar.ToolBar;
import com.revolsys.util.Property;

public class FieldNamesSetPanel extends ValueField
  implements ActionListener, PropertyChangeListener {

  private static final long serialVersionUID = 1L;

  public static String showDialog(final AbstractRecordLayer layer) {
    final FieldNamesSetPanel dialog = new FieldNamesSetPanel(layer);
    return dialog.showDialog();
  }

  private final JButton addButton;

  private final JXList allFieldNames;

  private final ArrayListModel<String> allFieldNamesModel;

  private final StringContainsRowFilter allFieldNamesTextFilter;

  private final JButton deleteButton;

  private final ComboBox<String> fieldNamesSetNamesField;

  private final ArrayListComboBoxModel<String> fieldNamesSetNamesModel;

  private final Map<String, List<String>> fieldNamesSets;

  private final JPanel fieldsPanel;

  private final JPanel filterPanel;

  private final AbstractRecordLayer layer;

  private final JButton moveDownButton;

  private final JButton moveUpButton;

  private final JButton removeButton;

  private final JButton renameButton;

  private final JXList selectedFieldNames;

  private final ArrayListModel<String> selectedFieldNamesModel;

  public FieldNamesSetPanel(final AbstractRecordLayer layer) {
    super(new VerticalLayout(5), "fieldNamesSetName", layer.getFieldNamesSetName());

    this.layer = layer;
    this.fieldNamesSets = new HashMap<>(layer.getFieldNamesSets());

    final List<String> fieldNamesSetNames = this.layer.getFieldNamesSetNames();
    this.fieldNamesSetNamesField = ComboBox.newComboBox("fieldNamesSetName", fieldNamesSetNames);
    this.fieldNamesSetNamesModel = this.fieldNamesSetNamesField.getComboBoxModel();
    int maxLength = 3;
    for (final String name : fieldNamesSetNames) {
      maxLength = Math.max(maxLength, name.length());
    }
    this.fieldNamesSetNamesField
      .setMaximumSize(new Dimension(Math.max(300, maxLength * 11 + 40), 22));
    Property.addListener(this.fieldNamesSetNamesField, "fieldNamesSetName", this);

    final ToolBar toolBar = new ToolBar();
    toolBar.setOpaque(false);
    toolBar.addComponent("default", this.fieldNamesSetNamesField);
    this.renameButton = toolBar.addButtonTitleIcon("default", "Rename Field Set",
      "fields_filter:edit", () -> actionRename());
    this.deleteButton = toolBar.addButtonTitleIcon("default", "Delete Field Set",
      "fields_filter:delete", () -> actionDelete());
    toolBar.addButtonTitleIcon("default", "Add Field Set", "fields_filter:add", () -> actionAdd());

    add(toolBar);

    this.filterPanel = new JPanel(new HorizontalLayout(46));
    this.filterPanel.setOpaque(false);
    add(this.filterPanel);

    final SearchField allFieldNamesFilterField = new SearchField("allFieldNamesFilter");
    allFieldNamesFilterField.setPreferredSize(new Dimension(350, 25));
    allFieldNamesFilterField.addActionListener(this);
    this.filterPanel.add(allFieldNamesFilterField);

    this.fieldsPanel = new JPanel(new HorizontalLayout(5));
    this.fieldsPanel.setOpaque(false);

    this.allFieldNamesModel = new ArrayListModel<>(layer.getFieldNames());
    this.allFieldNames = new JXList(this.allFieldNamesModel);
    this.allFieldNames.setAutoCreateRowSorter(true);
    this.allFieldNames.setSortable(true);
    this.allFieldNames.setSortOrder(SortOrder.ASCENDING);
    this.allFieldNames.addListSelectionListener(event -> updateEnabledState());

    final JScrollPane layerPathsScrollPane = new JScrollPane(this.allFieldNames);
    layerPathsScrollPane.setPreferredSize(new Dimension(350, 400));
    this.fieldsPanel.add(layerPathsScrollPane);

    final ToolBar fieldsToolBar = new ToolBar(SwingConstants.VERTICAL);
    fieldsToolBar.setOpaque(false);
    fieldsToolBar.setMinimumSize(new Dimension(25, 25));
    this.fieldsPanel.add(fieldsToolBar);

    this.addButton = fieldsToolBar.addButtonTitleIcon("default", "Add", "add",
      () -> actionAddSelected());
    this.removeButton = fieldsToolBar.addButtonTitleIcon("default", "Remove", "delete",
      () -> actionRemoveSelected());

    this.moveUpButton = fieldsToolBar.addButtonTitleIcon("default", "Move Up", "arrow_up",
      () -> actionMoveSelectedUp());
    this.moveDownButton = fieldsToolBar.addButtonTitleIcon("default", "Move Down", "arrow_down",
      () -> actionMoveSelectedDown());

    this.selectedFieldNamesModel = new ArrayListModel<>();

    this.selectedFieldNames = new JXList(this.selectedFieldNamesModel);
    this.selectedFieldNames.setAutoCreateRowSorter(false);
    this.selectedFieldNames.setSortable(false);
    this.selectedFieldNames.addListSelectionListener(event -> updateEnabledState());
    this.selectedFieldNames.setDragEnabled(true);
    this.selectedFieldNames.setDropMode(DropMode.INSERT);
    this.selectedFieldNames
      .setTransferHandler(new ListReorderableTransferHandler(this.selectedFieldNames));

    final JScrollPane snapScrollPane = new JScrollPane(this.selectedFieldNames);
    snapScrollPane.setPreferredSize(new Dimension(350, 400));
    this.fieldsPanel.add(snapScrollPane);
    add(this.fieldsPanel);

    this.allFieldNamesTextFilter = new StringContainsRowFilter();
    final RowFilter<ListModel, Integer> allFieldNamesFilter = RowFilter.andFilter(Arrays.asList(
      new CollectionRowFilter(this.selectedFieldNamesModel, false), this.allFieldNamesTextFilter));
    this.allFieldNames.setRowFilter(allFieldNamesFilter);

    final String fieldNamesSetName = layer.getFieldNamesSetName();
    setFieldNamesSetName(fieldNamesSetName);
    updateEnabledState();
  }

  private void actionAdd() {
    final String name = Dialogs.showInputDialog("Enter the name of the new field set.",
      "Add Field Set", JOptionPane.PLAIN_MESSAGE);
    if (Property.hasValue(name)) {
      boolean found = false;
      for (int i = 0; i < this.fieldNamesSetNamesModel.size(); i++) {
        final String name2 = this.fieldNamesSetNamesModel.get(i);
        if (name2.equalsIgnoreCase(name)) {
          this.fieldNamesSetNamesModel.set(i, name);
          final List<String> names = this.fieldNamesSets.remove(name2);
          this.fieldNamesSets.put(name, names);
          found = true;
        }
      }
      if (!found) {
        this.fieldNamesSetNamesModel.add(name);
        this.fieldNamesSetNamesModel.setSelectedItem(name);
      }
    }
  }

  private void actionAddSelected() {
    this.selectedFieldNames.clearSelection();
    int firstIndex = Integer.MAX_VALUE;
    for (final Object selectedValue : this.allFieldNames.getSelectedValues()) {
      int allIndex = this.allFieldNamesModel.indexOf(selectedValue);
      if (allIndex >= 0) {
        allIndex = this.allFieldNames.convertIndexToView(allIndex);
        if (allIndex < firstIndex) {
          firstIndex = allIndex;
        }
      }
      final String fieldName = (String)selectedValue;
      if (!this.selectedFieldNamesModel.contains(fieldName)) {
        this.selectedFieldNamesModel.add(fieldName);
        final int index = this.selectedFieldNames
          .convertIndexToView(this.selectedFieldNamesModel.indexOf(fieldName));
        this.selectedFieldNames.addSelectionInterval(index, index);
      }
      this.allFieldNamesModel.remove(fieldName);
    }
    if (firstIndex == Integer.MAX_VALUE) {
      firstIndex = 0;
    } else if (firstIndex >= this.allFieldNamesModel.size()) {
      firstIndex = this.allFieldNamesModel.size() - 1;
    }
    this.allFieldNames.setSelectedIndex(firstIndex);
    updateEnabledState();
  }

  private void actionDelete() {
    final String fieldSetName = this.fieldNamesSetNamesModel.getSelectedItem();
    if ("All".equalsIgnoreCase(fieldSetName)) {
      SwingUtil.beep();
    } else {
      final int result = Dialogs.showConfirmDialog("Delete field set " + fieldSetName + ".",
        "Delete Field Set", JOptionPane.YES_NO_OPTION);
      if (result == JOptionPane.YES_OPTION) {
        for (int i = 0; i < this.fieldNamesSetNamesModel.size(); i++) {
          final String name2 = this.fieldNamesSetNamesModel.get(i);
          if (fieldSetName.equalsIgnoreCase(name2)) {
            this.fieldNamesSetNamesModel.remove(i);
            this.fieldNamesSets.remove(name2);
          }
        }
        this.fieldNamesSetNamesField.setSelectedItem("All");
      }
    }
  }

  private void actionMoveSelectedDown() {
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

  private void actionMoveSelectedUp() {
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

  private void actionRemoveSelected() {
    final Object[] selectedValues = this.selectedFieldNames.getSelectedValues();
    int firstIndex = Integer.MAX_VALUE;
    for (final Object selectedValue : selectedValues) {
      final int index = this.selectedFieldNamesModel.indexOf(selectedValue);
      if (index != -1) {
        if (index < firstIndex) {
          firstIndex = index;
        }
      }
    }
    this.selectedFieldNamesModel.removeAll(selectedValues);

    if (firstIndex == Integer.MAX_VALUE) {
      firstIndex = 0;
    } else if (firstIndex >= this.selectedFieldNamesModel.size()) {
      firstIndex = this.selectedFieldNamesModel.size() - 1;
    }
    this.selectedFieldNames.setSelectedIndex(firstIndex);

    for (final Object selectedValue : selectedValues) {
      final String fieldName = (String)selectedValue;
      this.allFieldNamesModel.add(fieldName);
    }
    sort(this.allFieldNames);
    updateEnabledState();
  }

  private void actionRename() {
    final String oldName = this.fieldNamesSetNamesModel.getSelectedItem();
    if ("All".equalsIgnoreCase(oldName)) {
      SwingUtil.beep();
    } else {
      final String newName = (String)Dialogs.showInputDialog(
        "Enter the new name for the field set.", "Rename Field Set", JOptionPane.PLAIN_MESSAGE,
        null, null, oldName);
      if (Property.hasValue(newName)) {
        int index = -1;
        for (int i = 0; i < this.fieldNamesSetNamesModel.size(); i++) {
          final String name = this.fieldNamesSetNamesModel.get(i);
          if (oldName.equalsIgnoreCase(name)) {
            index = i;
          } else if (newName.equalsIgnoreCase(name)) {
            Dialogs.showMessageDialog("New name already in use: " + newName, "Rename Field Set",
              JOptionPane.ERROR_MESSAGE);
            return;
          }
        }

        this.fieldNamesSets.put(newName, new ArrayList<>(this.selectedFieldNamesModel));
        this.fieldNamesSetNamesModel.set(index, newName);
        this.fieldNamesSetNamesModel.setSelectedItem(newName);
      }
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final String propertyName = event.getPropertyName();
    if (propertyName.equals("fieldNamesSetName")) {
      final String oldFieldNamesSetName = (String)event.getOldValue();
      if (oldFieldNamesSetName != null) {
        this.fieldNamesSets.put(oldFieldNamesSetName,
          new ArrayList<>(this.selectedFieldNamesModel));
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
    final String fieldNamesSetName = this.fieldNamesSetNamesField.getSelectedItem();
    this.fieldNamesSets.put(fieldNamesSetName, namesToSave);

    final Map<String, List<String>> fieldNamesSets = new LinkedHashMap<>();
    for (final String name : this.fieldNamesSetNamesModel) {
      final List<String> fieldNames = this.fieldNamesSets.get(name);
      if (Property.hasValue(fieldNames)) {
        fieldNamesSets.put(name, fieldNames);
      }
    }
    this.layer.setFieldNamesSets(fieldNamesSets);
  }

  public void setFieldNamesSetName(String fieldNamesSetName) {
    if (Property.isEmpty(fieldNamesSetName)
      || !this.fieldNamesSetNamesModel.contains(fieldNamesSetName)) {
      fieldNamesSetName = "All";
    }
    super.setFieldValue(fieldNamesSetName);
    final List<String> allFieldNames = new ArrayList<>(this.layer.getFieldNames());
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
    this.fieldNamesSetNamesField.setSelectedItem(fieldNamesSetName);
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
    final String fieldSetName = this.fieldNamesSetNamesModel.getSelectedItem();
    final boolean editEnabled = !"All".equalsIgnoreCase(fieldSetName);
    this.deleteButton.setEnabled(editEnabled);
    this.renameButton.setEnabled(editEnabled);

    this.addButton.setEnabled(this.allFieldNames.getSelectedIndex() > -1);
    final int selectedFieldIndex = this.selectedFieldNames.getSelectedIndex();
    final int lastSelectedFieldIndex = this.selectedFieldNames.getSelectionModel()
      .getMaxSelectionIndex();
    this.removeButton.setEnabled(editEnabled && selectedFieldIndex > -1);
    this.moveUpButton.setEnabled(selectedFieldIndex > 0);
    this.moveDownButton.setEnabled(selectedFieldIndex > -1
      && lastSelectedFieldIndex < this.selectedFieldNamesModel.getSize() - 1);
  }
}
