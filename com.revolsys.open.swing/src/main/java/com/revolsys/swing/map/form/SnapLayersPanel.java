package com.revolsys.swing.map.form;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.DefaultRowSorter;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;
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
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.field.SearchField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.list.BaseListModel;
import com.revolsys.swing.list.filter.StringContainsRowFilter;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.toolbar.ToolBar;

public class SnapLayersPanel extends ValueField implements ActionListener,
  ListSelectionListener {

  private static final long serialVersionUID = 1L;

  private final AbstractDataObjectLayer layer;

  private final CheckBox snapToAllLayers;

  private final JXList snapLayerPathsField;

  private final JXList layerPathsField;

  private final BaseListModel<String> snapLayerPathsModel;

  private final BaseListModel<String> layerPathsModel;

  private final StringContainsRowFilter layerPathsTextFilter;

  private final StringContainsRowFilter snapLayerPathsTextFilter;

  private final JPanel filterPanel;

  private final JPanel fieldsPanel;

  private final JButton addButton;

  private final JButton removeButton;

  @SuppressWarnings("unchecked")
  public SnapLayersPanel(final AbstractDataObjectLayer layer) {
    super(new VerticalLayout(5));
    this.layer = layer;

    SwingUtil.setTitledBorder(this, "Snapping");
    final JPanel snapAllPanel = new JPanel();
    snapAllPanel.setOpaque(false);
    add(snapAllPanel);

    SwingUtil.addLabel(snapAllPanel, "Snap To All Visible Layers");
    snapToAllLayers = new CheckBox("snapToAllLayers", layer.isSnapToAllLayers());
    snapToAllLayers.addActionListener(this);
    snapAllPanel.add(snapToAllLayers);
    GroupLayoutUtil.makeColumns(snapAllPanel, 2, false);

    filterPanel = new JPanel(new HorizontalLayout(46));
    filterPanel.setOpaque(false);
    add(filterPanel);

    final SearchField layerPathsFilterField = new SearchField(
      "layerPathsFilter");
    layerPathsFilterField.setPreferredSize(new Dimension(350, 25));
    layerPathsFilterField.addActionListener(this);
    filterPanel.add(layerPathsFilterField);

    final SearchField snapLayerPathsFilterField = new SearchField(
      "snapLayerPathsFilter");
    snapLayerPathsFilterField.setPreferredSize(new Dimension(350, 25));
    snapLayerPathsFilterField.addActionListener(this);
    filterPanel.add(snapLayerPathsFilterField);

    fieldsPanel = new JPanel(new HorizontalLayout(5));
    fieldsPanel.setOpaque(false);

    final List<AbstractDataObjectLayer> dataObjectLayers = layer.getProject()
      .getDescenants(AbstractDataObjectLayer.class);

    layerPathsModel = new BaseListModel<String>();
    for (final AbstractDataObjectLayer dataObjectLayer : dataObjectLayers) {
      final String layerPath = dataObjectLayer.getPath();
      layerPathsModel.add(layerPath);
    }
    layerPathsField = new JXList(layerPathsModel);
    layerPathsField.setAutoCreateRowSorter(true);
    layerPathsField.setSortable(true);
    layerPathsField.setSortOrder(SortOrder.ASCENDING);
    layerPathsField.addListSelectionListener(this);
    final JScrollPane layerPathsScrollPane = new JScrollPane(layerPathsField);
    layerPathsScrollPane.setPreferredSize(new Dimension(350, 400));
    fieldsPanel.add(layerPathsScrollPane);

    final ToolBar toolBar = new ToolBar(ToolBar.VERTICAL);
    toolBar.setMinimumSize(new Dimension(25, 25));
    fieldsPanel.add(toolBar);

    addButton = toolBar.addButtonTitleIcon("default", "Add", "add", this,
      "addSelected");
    removeButton = toolBar.addButtonTitleIcon("default", "Remove", "delete",
      this, "removeSelected");

    final Collection<String> snapLayerPaths = layer.getSnapLayerPaths();
    snapLayerPathsModel = new BaseListModel<String>(snapLayerPaths);

    snapLayerPathsField = new JXList(snapLayerPathsModel);
    snapLayerPathsField.setAutoCreateRowSorter(true);
    snapLayerPathsField.setSortable(true);
    snapLayerPathsField.setSortOrder(SortOrder.ASCENDING);
    snapLayerPathsField.addListSelectionListener(this);
    snapLayerPathsTextFilter = new StringContainsRowFilter();
    snapLayerPathsField.setRowFilter(snapLayerPathsTextFilter);

    final JScrollPane snapScrollPane = new JScrollPane(snapLayerPathsField);
    snapScrollPane.setPreferredSize(new Dimension(350, 400));
    fieldsPanel.add(snapScrollPane);
    add(fieldsPanel);

    layerPathsTextFilter = new StringContainsRowFilter();
    final RowFilter<ListModel, Integer> layerPathsFilter = RowFilter.andFilter(Arrays.asList(
      new CollectionRowFilter(snapLayerPathsModel, false), layerPathsTextFilter));
    layerPathsField.setRowFilter(layerPathsFilter);
    updateEnabledState();
  }

  @Override
  public void actionPerformed(final ActionEvent event) {
    final Object source = event.getSource();
    if (source instanceof SearchField) {
      final SearchField field = (SearchField)source;
      final String fieldName = field.getFieldName();
      final String text = field.getText();
      if (fieldName.equals("layerPathsFilter")) {
        layerPathsTextFilter.setFilterText(text);
        sort(layerPathsField);
      } else if (fieldName.equals("snapLayerPathsFilter")) {
        snapLayerPathsTextFilter.setFilterText(text);
        sort(snapLayerPathsField);
      }
    } else if (source == snapToAllLayers) {
      updateEnabledState();
    }
  }

  public void addSelected() {
    snapLayerPathsField.clearSelection();
    for (final Object selectedValue : layerPathsField.getSelectedValues()) {
      final String layerPath = (String)selectedValue;
      if (!snapLayerPathsModel.contains(layerPath)) {
        snapLayerPathsModel.add(layerPath);
        final RowSorter<? extends ListModel> rowSorter = layerPathsField.getRowSorter();
        if (rowSorter instanceof DefaultRowSorter) {
          final DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>)rowSorter;
          sorter.sort();
        }
        final int index = snapLayerPathsField.convertIndexToView(snapLayerPathsModel.indexOf(layerPath));
        snapLayerPathsField.addSelectionInterval(index, index);
      }
    }
  }

  public void removeSelected() {
    final Object[] selectedValues = snapLayerPathsField.getSelectedValues();
    snapLayerPathsModel.removeAll(selectedValues);
    sort(layerPathsField);
  }

  @Override
  public void save() {
    super.save();
    layer.setSnapToAllLayers(snapToAllLayers.isSelected());
    final Set<String> layerPaths = new TreeSet<String>();
    for (int i = 0; i < snapLayerPathsModel.size(); i++) {
      final String layerPath = snapLayerPathsModel.get(i);
      layerPaths.add(layerPath);
    }
    layer.setSnapLayerPaths(layerPaths);
  }

  public void sort(final JXList list) {
    final RowSorter<? extends ListModel> rowSorter = list.getRowSorter();
    if (rowSorter instanceof DefaultRowSorter) {
      final DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>)rowSorter;
      sorter.sort();
    }
  }

  public void updateEnabledState() {
    final boolean enabled = !snapToAllLayers.isSelected();
    SwingUtil.setDescendantsEnabled(filterPanel, enabled);
    SwingUtil.setDescendantsEnabled(fieldsPanel, enabled);
    if (enabled) {
      addButton.setEnabled(layerPathsField.getSelectedIndex() > -1);
      removeButton.setEnabled(snapLayerPathsField.getSelectedIndex() > -1);
    }
  }

  @Override
  public void valueChanged(final ListSelectionEvent event) {
    updateEnabledState();
  }
}
