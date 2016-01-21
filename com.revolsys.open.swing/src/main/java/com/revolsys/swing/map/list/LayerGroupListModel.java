package com.revolsys.swing.map.list;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractListModel;

import com.revolsys.swing.field.BaseComboBoxModel;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.util.Property;
import com.revolsys.util.Reorderable;

public class LayerGroupListModel extends AbstractListModel<Layer>
  implements BaseComboBoxModel<Layer>, Reorderable, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final boolean allowNull;

  private final LayerGroup group;

  private Layer selectedItem;

  public LayerGroupListModel(final LayerGroup group) {
    this(group, false);
  }

  public LayerGroupListModel(final LayerGroup group, final boolean allowNull) {
    this.group = group;
    Property.addListener(group, this);
    this.allowNull = allowNull;
  }

  @Override
  public Layer getElementAt(int index) {
    if (this.allowNull) {
      if (index == 0) {
        return NullLayer.INSTANCE;
      }
      index--;
    }
    return this.group.getLayer(index);
  }

  @Override
  public Object getSelectedItem() {
    return this.selectedItem;
  }

  @Override
  public int getSize() {
    int size = this.group.getLayerCount();
    if (this.allowNull) {
      size++;
    }
    return size;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getSource() == this.group) {
      final String propertyName = event.getPropertyName();
      if (propertyName.equals("layers")) {
        final Object oldValue = event.getOldValue();
        final Object newValue = event.getNewValue();
        if (event instanceof IndexedPropertyChangeEvent) {
          final IndexedPropertyChangeEvent indexedEvent = (IndexedPropertyChangeEvent)event;
          final int index = indexedEvent.getIndex();
          if (oldValue == null) {
            if (newValue != null) {
              fireIntervalAdded(this.group, index, index);
            }
          }
          if (newValue == null) {
            fireIntervalRemoved(this.group, index, index);
          } else {
            fireContentsChanged(this.group, index, index);
          }
        }
      }
    }
  }

  @Override
  public void reorder(final int fromIndex, int toIndex) {
    if (fromIndex < toIndex) {
      toIndex--;
    }
    final Layer layer = getElementAt(fromIndex);
    this.group.removeLayer(fromIndex);
    this.group.addLayer(toIndex, layer);
  }

  @Override
  public void setSelectedItem(final Object selectedItem) {
    if (selectedItem instanceof Layer) {
      final Layer layer = (Layer)selectedItem;
      if (this.selectedItem != layer) {
        this.selectedItem = layer;
        final int index = this.group.indexOf(layer);
        fireContentsChanged(layer, index, index);
      }
    }
  }
}
