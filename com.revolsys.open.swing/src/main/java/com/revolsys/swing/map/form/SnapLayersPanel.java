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
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXList;
import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.swing.Borders;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.field.SearchField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.list.ArrayListModel;
import com.revolsys.swing.list.filter.StringContainsRowFilter;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.toolbar.ToolBar;

public class SnapLayersPanel extends ValueField implements ActionListener, ListSelectionListener {

  private static final long serialVersionUID = 1L;

  private final JButton addButton;

  private final JPanel fieldsPanel;

  private final JPanel filterPanel;

  private final AbstractRecordLayer layer;

  private final JXList layerPathsField;

  private final ArrayListModel<String> layerPathsModel;

  private final StringContainsRowFilter layerPathsTextFilter;

  private final JButton removeButton;

  private final JXList snapLayerPathsField;

  private final ArrayListModel<String> snapLayerPathsModel;

  private final StringContainsRowFilter snapLayerPathsTextFilter;

  private final CheckBox snapToAllLayers;

  public SnapLayersPanel(final AbstractRecordLayer layer) {
    super(new VerticalLayout(5));
    this.layer = layer;

    Borders.titled(this, "Snapping");
    final JPanel snapAllPanel = new JPanel();
    snapAllPanel.setOpaque(false);
    add(snapAllPanel);

    SwingUtil.addLabel(snapAllPanel, "Snap To All Visible Layers");
    this.snapToAllLayers = new CheckBox("snapToAllLayers", layer.isSnapToAllLayers());
    this.snapToAllLayers.addActionListener(this);
    snapAllPanel.add(this.snapToAllLayers);
    GroupLayouts.makeColumns(snapAllPanel, 2, false);

    this.filterPanel = new JPanel(new HorizontalLayout(46));
    this.filterPanel.setOpaque(false);
    add(this.filterPanel);

    final SearchField layerPathsFilterField = new SearchField("layerPathsFilter");
    layerPathsFilterField.setPreferredSize(new Dimension(350, 25));
    layerPathsFilterField.addActionListener(this);
    this.filterPanel.add(layerPathsFilterField);

    final SearchField snapLayerPathsFilterField = new SearchField("snapLayerPathsFilter");
    snapLayerPathsFilterField.setPreferredSize(new Dimension(350, 25));
    snapLayerPathsFilterField.addActionListener(this);
    this.filterPanel.add(snapLayerPathsFilterField);

    this.fieldsPanel = new JPanel(new HorizontalLayout(5));
    this.fieldsPanel.setOpaque(false);

    final List<AbstractRecordLayer> recordLayers = layer.getProject()
      .getDescenants(AbstractRecordLayer.class);

    this.layerPathsModel = new ArrayListModel<>();
    for (final AbstractLayer recordLayer : recordLayers) {
      if (recordLayer.isHasGeometry()) {
        final String layerPath = recordLayer.getPath();
        this.layerPathsModel.add(layerPath);
      }
    }
    this.layerPathsField = new JXList(this.layerPathsModel);
    this.layerPathsField.setAutoCreateRowSorter(true);
    this.layerPathsField.setSortable(true);
    this.layerPathsField.setSortOrder(SortOrder.ASCENDING);
    this.layerPathsField.addListSelectionListener(this);
    final JScrollPane layerPathsScrollPane = new JScrollPane(this.layerPathsField);
    layerPathsScrollPane.setPreferredSize(new Dimension(350, 400));
    this.fieldsPanel.add(layerPathsScrollPane);

    final ToolBar toolBar = new ToolBar(SwingConstants.VERTICAL);
    toolBar.setOpaque(false);
    toolBar.setMinimumSize(new Dimension(25, 25));
    this.fieldsPanel.add(toolBar);

    this.addButton = toolBar.addButtonTitleIcon("default", "Add", "add", this::addSelected);
    this.removeButton = toolBar.addButtonTitleIcon("default", "Remove", "delete",
      this::removeSelected);

    final Collection<String> snapLayerPaths = layer.getSnapLayerPaths();
    this.snapLayerPathsModel = new ArrayListModel<>(snapLayerPaths);

    this.snapLayerPathsField = new JXList(this.snapLayerPathsModel);
    this.snapLayerPathsField.setAutoCreateRowSorter(true);
    this.snapLayerPathsField.setSortable(true);
    this.snapLayerPathsField.setSortOrder(SortOrder.ASCENDING);
    this.snapLayerPathsField.addListSelectionListener(this);
    this.snapLayerPathsTextFilter = new StringContainsRowFilter();
    this.snapLayerPathsField.setRowFilter(this.snapLayerPathsTextFilter);

    final JScrollPane snapScrollPane = new JScrollPane(this.snapLayerPathsField);
    snapScrollPane.setPreferredSize(new Dimension(350, 400));
    this.fieldsPanel.add(snapScrollPane);
    add(this.fieldsPanel);

    this.layerPathsTextFilter = new StringContainsRowFilter();
    final RowFilter<ListModel, Integer> layerPathsFilter = RowFilter.andFilter(Arrays
      .asList(new CollectionRowFilter(this.snapLayerPathsModel, false), this.layerPathsTextFilter));
    this.layerPathsField.setRowFilter(layerPathsFilter);
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
        this.layerPathsTextFilter.setFilterText(text);
        sort(this.layerPathsField);
      } else if (fieldName.equals("snapLayerPathsFilter")) {
        this.snapLayerPathsTextFilter.setFilterText(text);
        sort(this.snapLayerPathsField);
      }
    } else if (source == this.snapToAllLayers) {
      updateEnabledState();
    }
  }

  public void addSelected() {
    this.snapLayerPathsField.clearSelection();
    for (final Object selectedValue : this.layerPathsField.getSelectedValues()) {
      final String layerPath = (String)selectedValue;
      if (!this.snapLayerPathsModel.contains(layerPath)) {
        this.snapLayerPathsModel.add(layerPath);
        final RowSorter<? extends ListModel> rowSorter = this.layerPathsField.getRowSorter();
        if (rowSorter instanceof DefaultRowSorter) {
          final DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>)rowSorter;
          sorter.sort();
        }
        final int index = this.snapLayerPathsField
          .convertIndexToView(this.snapLayerPathsModel.indexOf(layerPath));
        this.snapLayerPathsField.addSelectionInterval(index, index);
      }
    }
  }

  public void removeSelected() {
    final Object[] selectedValues = this.snapLayerPathsField.getSelectedValues();
    this.snapLayerPathsModel.removeAll(selectedValues);
    sort(this.layerPathsField);
  }

  @Override
  public void save() {
    super.save();
    this.layer.setSnapToAllLayers(this.snapToAllLayers.isSelected());
    final Set<String> layerPaths = new TreeSet<>();
    for (int i = 0; i < this.snapLayerPathsModel.size(); i++) {
      final String layerPath = this.snapLayerPathsModel.get(i);
      layerPaths.add(layerPath);
    }
    this.layer.setSnapLayerPaths(layerPaths);
  }

  public void sort(final JXList list) {
    final RowSorter<? extends ListModel> rowSorter = list.getRowSorter();
    if (rowSorter instanceof DefaultRowSorter) {
      final DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>)rowSorter;
      sorter.sort();
    }
  }

  public void updateEnabledState() {
    final boolean enabled = !this.snapToAllLayers.isSelected();
    SwingUtil.setDescendantsEnabled(this.filterPanel, enabled);
    SwingUtil.setDescendantsEnabled(this.fieldsPanel, enabled);
    if (enabled) {
      this.addButton.setEnabled(this.layerPathsField.getSelectedIndex() > -1);
      this.removeButton.setEnabled(this.snapLayerPathsField.getSelectedIndex() > -1);
    }
  }

  @Override
  public void valueChanged(final ListSelectionEvent event) {
    updateEnabledState();
  }
}
