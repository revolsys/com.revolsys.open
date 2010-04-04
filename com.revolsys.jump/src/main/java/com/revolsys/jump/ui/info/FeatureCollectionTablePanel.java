package com.revolsys.jump.ui.info;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.NoninvertibleTransformException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

import com.revolsys.jump.ui.swing.FeatureTypeUiBuilderRegistry;
import com.revolsys.jump.ui.swing.table.FeatureListTableModel;
import com.revolsys.jump.ui.swing.table.FeatureTableFactory;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollectionWrapper;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.WorkbenchContext;
import com.vividsolutions.jump.workbench.model.CategoryEvent;
import com.vividsolutions.jump.workbench.model.FeatureEvent;
import com.vividsolutions.jump.workbench.model.FeatureEventType;
import com.vividsolutions.jump.workbench.model.Layer;
import com.vividsolutions.jump.workbench.model.LayerEvent;
import com.vividsolutions.jump.workbench.model.LayerEventType;
import com.vividsolutions.jump.workbench.model.LayerListener;
import com.vividsolutions.jump.workbench.model.LayerManager;
import com.vividsolutions.jump.workbench.ui.ErrorHandler;
import com.vividsolutions.jump.workbench.ui.FeatureSelection;
import com.vividsolutions.jump.workbench.ui.LayerTableModel;
import com.vividsolutions.jump.workbench.ui.LayerViewPanel;
import com.vividsolutions.jump.workbench.ui.SelectionManager;
import com.vividsolutions.jump.workbench.ui.images.IconLoader;
import com.vividsolutions.jump.workbench.ui.zoom.ZoomToSelectedItemsPlugIn;

@SuppressWarnings("serial")
public class FeatureCollectionTablePanel extends JPanel {
  private class TableHeaderCellRenderer implements TableCellRenderer {

    private Icon clearIcon = IconLoader.icon("Clear.gif");

    private TableCellRenderer defaultTableCellRenderer;

    private Icon downIcon = IconLoader.icon("Down.gif");

    private Icon upIcon = IconLoader.icon("Up.gif");

    public TableHeaderCellRenderer(
      final TableCellRenderer defaultTableCellRenderer) {
      super();
      this.defaultTableCellRenderer = defaultTableCellRenderer;
    }

    public Component getTableCellRendererComponent(
      final JTable table,
      final Object value,
      final boolean isSelected,
      final boolean hasFocus,
      final int row,
      final int column) {
      JLabel label = (JLabel)defaultTableCellRenderer.getTableCellRendererComponent(
        table, value, isSelected, hasFocus, row, column);
      if (tableModel.getSortedColumnIndex() != column) {
        label.setIcon(clearIcon);
      } else if (tableModel.isSortAscending()) {
        label.setIcon(upIcon);
      } else {
        label.setIcon(downIcon);
      }
      label.setHorizontalTextPosition(SwingConstants.LEFT);
      return label;
    }
  }

  private Layer layer;

  private JScrollPane scrollPane;

  private JTable table;

  private FeatureListTableModel tableModel;

  private FeatureTypeUiBuilderRegistry uiBuilderRegistry;

  private WorkbenchContext workbenchContext;

  private ZoomToSelectedItemsPlugIn zoomToSelectedItemsPlugIn = new ZoomToSelectedItemsPlugIn();

  private LayerListener layerListener;

  public FeatureCollectionTablePanel(
    final WorkbenchContext workbenchContext) {
    super(new BorderLayout());
    this.workbenchContext = workbenchContext;
    uiBuilderRegistry = FeatureTypeUiBuilderRegistry.getInstance(workbenchContext);

    scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
      JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    this.add(scrollPane, BorderLayout.CENTER);
  }

  public void flashSelectedFeatures()
    throws NoninvertibleTransformException {
    List<Feature> features = getSelectedFeatures();
    List<Geometry> geometries = getGeometries(features);

    LayerViewPanel layerViewPanel = workbenchContext.getLayerViewPanel();
    zoomToSelectedItemsPlugIn.flash(geometries, layerViewPanel);
  }

  private List<Geometry> getGeometries(
    final List<Feature> features) {
    List<Geometry> geometries = new ArrayList<Geometry>();
    for (Feature feature : features) {
      Geometry geometry = feature.getGeometry();
      geometries.add(geometry);
    }
    return geometries;
  }

  public List<Feature> getSelectedFeatures() {
    int[] selectedRows = table.getSelectedRows();
    List<Feature> features = tableModel.getFeatures(selectedRows);
    return features;
  }

  public JTable getTable() {
    return table;
  }

  public boolean isAtFirst() {
    return table.getSelectedRow() == 0;
  }

  public boolean isAtLast() {
    return table.getSelectedRow() == table.getRowCount() - 1;
  }

  public boolean isRowSelected() {
    int selectedRow = table.getSelectedRow();
    return selectedRow != -1;
  }

  public void selectInLayerViewPanel() {
    List<Feature> features = getSelectedFeatures();

    LayerViewPanel layerViewPanel = workbenchContext.getLayerViewPanel();
    SelectionManager selectionManager = layerViewPanel.getSelectionManager();
    selectionManager.clear();
    FeatureSelection featureSelection = selectionManager.getFeatureSelection();

    featureSelection.selectItems(layer, features);
  }

  private LayerManager setFeatures(
    final Layer layer,
    final FeatureCollectionWrapper featureCollection,
    final List<Feature> features) {
    final LayerManager layerManager = layer.getLayerManager();
    this.layer = layer;

    FeatureSchema featureSchema = featureCollection.getFeatureSchema();
    tableModel = new FeatureListTableModel(layer, featureSchema, features);
    tableModel.addPropertyChangeListener(new PropertyChangeListener() {

      public void propertyChange(
        final PropertyChangeEvent event) {
        String name = event.getPropertyName();
        Object oldValue = event.getOldValue();

        Feature feature = (Feature)event.getSource();
        Feature oldFeature = feature.clone(true);
        oldFeature.setAttribute(name, oldValue);

        Set<Feature> newFeatures = Collections.singleton(feature);
        Set<Feature> oldFeatures = Collections.singleton(oldFeature);

        FeatureSchema schema = feature.getSchema();
        if (schema.getGeometryIndex() == schema.getAttributeIndex(name)) {
          layerManager.fireFeaturesAttChanged(newFeatures,
            FeatureEventType.GEOMETRY_MODIFIED, layer, oldFeatures);
        } else {
          layerManager.fireFeaturesAttChanged(newFeatures,
            FeatureEventType.ATTRIBUTES_MODIFIED, layer, oldFeatures);
        }
      }
    });

    table = FeatureTableFactory.createTable(tableModel, uiBuilderRegistry);
    scrollPane.setViewportView(table);
    JTableHeader tableHeader = table.getTableHeader();
    tableHeader.addMouseListener(new MouseAdapter() {

      public void mouseClicked(
        final MouseEvent e) {
        try {
          int column = table.columnAtPoint(e.getPoint());
          if (SwingUtilities.isLeftMouseButton(e)) {
            tableModel.sort(column);
          }
        } catch (Throwable t) {
          workbenchContext.getErrorHandler().handleThrowable(t);
        }
      }
    });
    tableHeader.setDefaultRenderer(new TableHeaderCellRenderer(
      tableHeader.getDefaultRenderer()));
    return layerManager;
  }

  @SuppressWarnings("unchecked")
  public void setLayer(
    final Layer layer) {
    FeatureCollectionWrapper featureCollection = layer.getFeatureCollectionWrapper();
    List<Feature> features = featureCollection.getFeatures();
    final LayerManager layerManager = setFeatures(layer, featureCollection,
      features);
    layerListener = new LayerListener() {
      public void categoryChanged(
        final CategoryEvent e) {
      }

      public void featuresChanged(
        final FeatureEvent e) {
        if (e.getLayer() == layer) {
          FeatureEventType type = e.getType();
          if (type == FeatureEventType.ADDED) {
            tableModel.fireTableDataChanged();
          } else if (type == FeatureEventType.DELETED) {
            Collection<Feature> features = e.getFeatures();
            tableModel.remove(features);
          } else {
            tableModel.fireTableDataChanged();
          }
        }
      }

      public void layerChanged(
        final LayerEvent e) {
        if (e.getLayerable() == layer) {
          if (e.getType() == LayerEventType.REMOVED) {
            tableModel.setFeatures(null);
          } else if (e.getType() == LayerEventType.METADATA_CHANGED) {
            tableModel.fireTableStructureChanged();
          } else if (e.getType() == LayerEventType.APPEARANCE_CHANGED) {
            tableModel.fireTableDataChanged();
          }
        }
      }

    };
    layerManager.addLayerListener(layerListener);
  }

  public void dispose() {
    if (layer != null) {
      layer.getLayerManager().removeLayerListener(layerListener);
    }
  }

  @SuppressWarnings("unchecked")
  public void setLayerTableModel(
    final LayerTableModel layerTableModel) {
    Layer layer = layerTableModel.getLayer();
    FeatureCollectionWrapper featureCollection = layer.getFeatureCollectionWrapper();
    List<Feature> features = layerTableModel.getFeatures();
    setFeatures(layer, featureCollection, features);
  }

  public void zoomTo(
    final Feature feature) {
    Geometry geometry = feature.getGeometry();
    zoomTo(geometry);
  }

  public void zoomTo(
    final Geometry geometry) {
    List<Geometry> geometries = Collections.singletonList(geometry);
    zoomToGeometries(geometries);
  }

  /**
   * Zoom to the geometries of the list of features.
   * 
   * @param features The list of features.
   */
  public void zoomToFeatures(
    final List<Feature> features) {
    List<Geometry> geometries = getGeometries(features);
    zoomToGeometries(geometries);
  }

  public void zoomToFirst() {
    if (table.getRowCount() > 0) {
      zoomToRow(0);
    }
  }

  public void zoomToGeometries(
    final List<Geometry> geometries) {
    try {
      LayerViewPanel layerViewPanel = workbenchContext.getLayerViewPanel();
      zoomToSelectedItemsPlugIn.zoom(geometries, layerViewPanel);
    } catch (NoninvertibleTransformException e) {
      ErrorHandler errorHandler = workbenchContext.getErrorHandler();
      errorHandler.handleThrowable(e);
    }
  }

  public void zoomToLast() {
    int rowCount = table.getRowCount();
    if (rowCount > 0) {
      zoomToRow(rowCount - 1);
    }
  }

  public void zoomToNext() {
    int index = table.getSelectedRow() + 1;
    if (index < table.getRowCount()) {
      zoomToRow(index);
      ListSelectionModel selectionModel = table.getSelectionModel();
      selectionModel.setSelectionInterval(index, index);
    }
  }

  public void zoomToPrevious() {
    int index = table.getSelectedRow() - 1;
    if (index >= 0) {
      zoomToRow(index);
    }
  }

  public void zoomToRow(
    final int index) {
    Feature feature = tableModel.getFeature(index);
    ListSelectionModel selectionModel = table.getSelectionModel();
    selectionModel.setSelectionInterval(index, index);
    Rectangle rect = table.getCellRect(index, 0, true);
    table.scrollRectToVisible(rect);
    zoomTo(feature);
  }

  public void zoomToRows(
    final int[] rows) {
    List<Feature> features = tableModel.getFeatures(rows);
    zoomToFeatures(features);
  }

  public void zoomToSelected() {
    zoomToRows(table.getSelectedRows());
  }
}
