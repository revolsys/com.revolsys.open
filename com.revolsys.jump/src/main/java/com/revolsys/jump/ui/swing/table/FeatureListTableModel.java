package com.revolsys.jump.ui.swing.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import com.revolsys.jump.ui.model.FeatureAttributeComparitor;
import com.revolsys.jump.ui.model.FeatureIdComparitor;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;
import com.vividsolutions.jump.workbench.model.Layer;

/**
 * A table model which displays the attributes of a single feature.
 * 
 * @author Paul Austin
 */
@SuppressWarnings("serial")
public class FeatureListTableModel extends AbstractTableModel {
  private int sortedColumnIndex = -1;

  private boolean sortAscending = true;

  private List<Feature> features = new ArrayList<Feature>();

  private FeatureSchema schema;

  private Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  private Layer layer;

  public FeatureListTableModel(final Layer layer, final FeatureSchema schema,
    final List<Feature> features) {
    this.layer = layer;
    this.schema = schema;
    this.features.addAll(features);
  }

  public int getColumnCount() {
    int geometryIndex = schema.getGeometryIndex();
    if (geometryIndex == -1) {
      return schema.getAttributeCount() + 1;
    } else {
      return schema.getAttributeCount();
    }
  }

  private int getAttributeIndex(final int column) {
    int attributeIndex = column - 1;
    int geometryIndex = schema.getGeometryIndex();
    if (geometryIndex != -1) {
      if (attributeIndex >= geometryIndex) {
        attributeIndex++;
      }
    }
    return attributeIndex;
  }

  public String getColumnName(final int column) {
    if (column == 0) {
      return "FID";
    } else {
      int attributeIndex = getAttributeIndex(column);
      return schema.getAttributeName(attributeIndex);
    }
  }

  public int getRowCount() {
    return features.size();
  }

  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 0) {
      return false;
    } else {
      return layer.isEditable();
    }
  }

  public Object getValueAt(final int rowIndex, final int columnIndex) {
    Feature feature = getFeature(rowIndex);
    if (feature == null) {
      return null;
    } else if (columnIndex == 0) {
      return feature.getID();
    } else {
      int attributeIndex = getAttributeIndex(columnIndex);
      return feature.getAttribute(attributeIndex);
    }
  }

  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    Feature feature = getFeature(rowIndex);
    if (feature != null) {
      if (columnIndex == 0) {
        throw new IllegalArgumentException("Cannot edit the feature ID");
      } else {
        int attributeIndex = getAttributeIndex(columnIndex);
        Object oldValue = feature.getAttribute(attributeIndex);
        String name = schema.getAttributeName(attributeIndex);
        feature.setAttribute(attributeIndex, value);
        firePropertyChange(feature, name, oldValue, value);
      }
    }
  }

  /**
   * @return the features
   */
  public List<Feature> getFeatures() {
    return features;
  }

  /**
   * @param features the features to set
   */
  public void setFeatures(final List<Feature> features) {
    this.features.clear();
    if (features != null) {
      this.features.addAll(features);
    }
    fireTableDataChanged();
  }

  public FeatureSchema getSchema() {
    return schema;
  }

  public void addPropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.add(propertyChangeListener);
  }

  public Set<PropertyChangeListener> getPropertyChangeListeners() {
    return Collections.unmodifiableSet(propertyChangeListeners);
  }

  public void removePropertyChangeListener(
    final PropertyChangeListener propertyChangeListener) {
    this.propertyChangeListeners.remove(propertyChangeListener);
  }

  private void firePropertyChange(final Feature feature, final String name,
    final Object oldValue, final Object newValue) {
    PropertyChangeEvent event = new PropertyChangeEvent(feature, name,
      oldValue, newValue);
    for (PropertyChangeListener listener : propertyChangeListeners) {
      listener.propertyChange(event);
    }
  }

  public Feature getFeature(final int index) {
    return features.get(index);
  }

  public List<Feature> getFeatures(final int[] rows) {
    List<Feature> features = new ArrayList<Feature>();
    for (int row : rows) {
      Feature feature = getFeature(row);
      features.add(feature);
    }
    return features;
  }

  public void remove(final Collection<Feature> removedFeatures) {
    if (this.features.removeAll(removedFeatures)) {
      fireTableDataChanged();
    }
  }

  public void sort(final int index) {
    if (index < getColumnCount()) {
      if (index == sortedColumnIndex) {
        sortAscending = !sortAscending;
      } else {
        sortAscending = true;
      }
      sortedColumnIndex = index;
      Comparator<Feature> comparitor;
      if (index == 0) {
        comparitor = new FeatureIdComparitor(sortAscending);
      } else {
        int attributeIndex = getAttributeIndex(index);
        comparitor = new FeatureAttributeComparitor(sortAscending,
          attributeIndex);
      }
      Collections.sort(features, comparitor);
      fireTableDataChanged();
    }
  }

  /**
   * @return the sortedColumnIndex
   */
  public int getSortedColumnIndex() {
    return sortedColumnIndex;
  }

  /**
   * @return the sortAscending
   */
  public boolean isSortAscending() {
    return sortAscending;
  }

}
