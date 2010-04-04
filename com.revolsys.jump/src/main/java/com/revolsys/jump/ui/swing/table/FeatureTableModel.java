package com.revolsys.jump.ui.swing.table;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.table.AbstractTableModel;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jump.feature.AttributeType;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * A table model which displays the attributes of a single feature.
 * 
 * @author Paul Austin
 */
@SuppressWarnings("serial")
public class FeatureTableModel extends AbstractTableModel {
  private static final String[] COLUMN_NAMES = {
    "Attribute", "Value"
  };

  private List<FeatureTableRow> rows = new ArrayList<FeatureTableRow>();

  private Set<PropertyChangeListener> propertyChangeListeners = new LinkedHashSet<PropertyChangeListener>();

  public FeatureTableModel() {

  }

  public FeatureTableModel(final Feature feature) {
    setFeature(feature);
  }

  public int getColumnCount() {
    return 2;
  }

  public String getColumnName(final int column) {
    return COLUMN_NAMES[column];
  }

  public int getRowCount() {
    return rows.size();

  }

  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    if (columnIndex == 1 && rowIndex > 0) {
      return true;
    } else {
      return false;
    }
  }

  public Object getValueAt(final int rowIndex, final int columnIndex) {

    FeatureTableRow row = getFeatureTableRow(rowIndex);
    switch (columnIndex) {
      case 0:
        return row.getAttributeName();
      case 1:
        return row.getValue();
      default:
        return null;
    }
  }

  public FeatureTableRow getFeatureTableRow(final int rowIndex) {
    return rows.get(rowIndex);
  }

  public int getIndentAt(final int rowIndex) {
    FeatureTableRow row = getFeatureTableRow(rowIndex);
    return row.getIndent();
  }

  public void setValueAt(final Object value, final int rowIndex,
    final int columnIndex) {
    switch (rowIndex) {
      case 0:
        throw new IllegalArgumentException("Attribute Column not editable");
      case 1:
        FeatureTableRow row = getFeatureTableRow(rowIndex);
        Feature feature = row.getFeature();
        Object oldValue = row.getValue();
        String name = row.getAttributeName();
        row.setValue(value);
        firePropertyChange(feature, name, oldValue, value);
      default:
      break;
    }
  }

  public void setFeature(final Feature feature) {
    rows.clear();
    if (feature != null) {
      rows.add(new FeatureTableRow(0, feature, "FID", feature.getID(), -1));

      addFeature(0, feature);
    }
    fireTableDataChanged();
  }

  @SuppressWarnings("unchecked")
  private void addFeature(final int indent, final Feature feature) {
    FeatureSchema schema = feature.getSchema();
    for (int i = 0; i < schema.getAttributeCount(); i++) {
      String attributeName = schema.getAttributeName(i);
      Object value = feature.getAttribute(i);
      if (schema.getAttributeType(i) != AttributeType.GEOMETRY) {
        rows.add(new FeatureTableRow(indent, feature, attributeName, value, i));
        if (value instanceof Feature) {
          Feature subFeature = (Feature)value;
          addFeature(indent + 1, subFeature);
        }
      }
    }
    if (feature.getSchema().getGeometryIndex() >= 0) {
      Geometry geometry = feature.getGeometry();
      if (geometry != null) {
        Object userData = geometry.getUserData();
        if (userData instanceof Map) {
          Map<Object, Object> map = (Map<Object, Object>)userData;
          rows.add(new FeatureTableRow(indent, null, "Geometry Info", null, -1));
          for (Entry<Object, Object> entry : map.entrySet()) {
            rows.add(new FeatureTableRow(indent + 1, null, entry.getKey()
              .toString(), entry.getValue(), -1));
          }

        }
        if (geometry instanceof GeometryCollection) {
          for (int i = 0; i < geometry.getNumGeometries(); i++) {
            Geometry subGeometry = geometry.getGeometryN(i);
            Object subUserData = subGeometry.getUserData();
            if (subUserData instanceof Map) {
              Map<Object, Object> map = (Map<Object, Object>)subUserData;
              rows.add(new FeatureTableRow(indent, null, "Geometry Info "
                + (i + 1), null, -1));
              for (Entry<Object, Object> entry : map.entrySet()) {
                rows.add(new FeatureTableRow(indent + 1, null, entry.getKey()
                  .toString(), entry.getValue(), -1));
              }

            }

          }
        }
      }
    }
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
}
