package com.revolsys.swing.map.list;

import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;

import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.NullLayer;
import com.revolsys.util.Reorderable;

public class LayerGroupListModel extends AbstractListModel implements
  ComboBoxModel, Reorderable, PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final LayerGroup group;

  private Layer selectedItem;

  private final boolean allowNull;

  public LayerGroupListModel(final LayerGroup group) {
    this(group, false);
  }

  public LayerGroupListModel(final LayerGroup group, final boolean allowNull) {
    this.group = group;
    group.addPropertyChangeListener(this);
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
    return this.group.get(index);
  }

  @Override
  public Object getSelectedItem() {
    return this.selectedItem;
  }

  @Override
  public int getSize() {
    int size = this.group.size();
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
    this.group.remove(fromIndex);
    this.group.add(toIndex, layer);
  }

  @Override
  public void setSelectedItem(final Object selectedItem) {
    if (selectedItem instanceof Layer) {
      final Layer layer = (Layer)selectedItem;
      this.selectedItem = layer;
    }
  }
}
