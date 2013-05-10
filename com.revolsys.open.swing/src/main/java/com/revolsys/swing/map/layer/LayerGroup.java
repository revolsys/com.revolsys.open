package com.revolsys.swing.map.layer;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.revolsys.gis.cs.BoundingBox;

public class LayerGroup extends AbstractLayer implements List<Layer> {

  private List<Layer> layers = new ArrayList<Layer>();

  public LayerGroup(final String name) {
    super(name);
    setRenderer(new LayerGroupRenderer(this));
  }

  @Override
  public void add(final int index, final Layer layer) {
    synchronized (layers) {
      if (layer != null && !layers.contains(layer)) {
        layers.add(index, layer);
        layer.setLayerGroup(this);
        final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
        propertyChangeSupport.fireIndexedPropertyChange("layers", index, null,
          layer);
      }
    }
  }

  @Override
  public boolean add(final Layer layer) {
    synchronized (layers) {
      if (layer == null || layers.contains(layer)) {
        return false;
      } else {
        final int index = layers.size();
        add(index, layer);
        return true;
      }
    }
  }

  @Override
  public boolean addAll(final Collection<? extends Layer> layers) {
    boolean added = false;
    for (final Layer layer : layers) {
      if (add(layer)) {
        added = true;
      }
    }
    return added;
  }

  @Override
  public boolean addAll(int index, final Collection<? extends Layer> c) {
    boolean added = false;
    for (final Layer layer : layers) {
      if (!layers.contains(layer)) {
        add(index, layer);
        added = true;
        index++;
      }
    }
    return added;
  }

  public LayerGroup addLayerGroup(final int index, final String name) {
    synchronized (layers) {
      final Layer layer = getLayer(name);
      if (layer == null) {
        final LayerGroup group = new LayerGroup(name);
        add(index, group);
        return group;
      }
      if (layer instanceof LayerGroup) {
        return (LayerGroup)layer;
      } else {
        throw new IllegalArgumentException("Layer exists with name " + name);
      }
    }
  }

  public LayerGroup addLayerGroup(final String name) {
    synchronized (layers) {
      final Layer layer = getLayer(name);
      if (layer == null) {
        final LayerGroup group = new LayerGroup(name);
        add(group);
        return group;
      }
      if (layer instanceof LayerGroup) {
        return (LayerGroup)layer;
      } else {
        throw new IllegalArgumentException("Layer exists with name " + name);
      }
    }
  }

  public void addPath(final List<Layer> path) {
    final LayerGroup layerGroup = getLayerGroup();
    if (layerGroup != null) {
      layerGroup.addPath(path);
    }
    path.add(this);
  }

  @Override
  public void clear() {
    final List<Layer> oldLayers = layers;
    layers = new ArrayList<Layer>();
    getPropertyChangeSupport().firePropertyChange("layers", oldLayers, layers);
  }

  public boolean contains(final Layer layer) {
    return layers.contains(layer);
  }

  @Override
  public boolean contains(final Object o) {
    return layers.contains(o);
  }

  @Override
  public boolean containsAll(final Collection<?> c) {
    return layers.containsAll(c);
  }

  @Override
  public Layer get(final int index) {
    return layers.get(index);
  }

  @Override
  public BoundingBox getBoundingBox() {
    BoundingBox boudingBox = new BoundingBox(getGeometryFactory());
    for (final Layer layer : this) {
      final BoundingBox layerBoundingBox = layer.getBoundingBox();
      if (!layerBoundingBox.isNull()) {
        boudingBox = boudingBox.expandToInclude(layerBoundingBox);
      }
    }
    return boudingBox;
  }

  @Override
  public BoundingBox getBoundingBox(final boolean visibleLayersOnly) {
    BoundingBox boudingBox = new BoundingBox(getGeometryFactory());
    if (!visibleLayersOnly || isVisible()) {
      for (final Layer layer : this) {
        if (!visibleLayersOnly || layer.isVisible()) {
          final BoundingBox layerBoundingBox = layer.getBoundingBox(visibleLayersOnly);
          if (!layerBoundingBox.isNull()) {
            boudingBox = boudingBox.expandToInclude(layerBoundingBox);
          }
        }
      }
    }
    return boudingBox;
  }

  @Override
  public long getId() {
    // TODO Auto-generated method stub
    return 0;
  }

  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final int i) {
    if (i < layers.size()) {
      return (V)layers.get(i);
    } else {
      return null;
    }
  }

  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final String name) {
    for (final Layer layer : layers) {
      if (layer.getName().equals(name)) {
        return (V)layer;
      }
    }
    return null;
  }

  public List<LayerGroup> getLayerGroups() {
    final List<LayerGroup> layerGroups = new ArrayList<LayerGroup>();
    for (final Layer layer : layers) {
      if (layer instanceof LayerGroup) {
        final LayerGroup layerGroup = (LayerGroup)layer;
        layerGroups.add(layerGroup);
      }
    }
    return layerGroups;
  }

  public List<Layer> getLayers() {
    return this;
  }

  @SuppressWarnings("unchecked")
  public <V extends Layer> List<V> getLayers(final Class<V> layerClass) {
    final List<V> layers = new ArrayList<V>();
    for (final Layer layer : this.layers) {
      if (layerClass.isAssignableFrom(layer.getClass())) {
        layers.add((V)layer);
      }
    }
    return layers;
  }

  @Override
  public BoundingBox getSelectedBoundingBox() {
    BoundingBox boundingBox = super.getSelectedBoundingBox();
    if (isVisible()) {
      for (final Layer layer : this) {
        final BoundingBox layerBoundingBox = layer.getSelectedBoundingBox();
        boundingBox = boundingBox.expandToInclude(layerBoundingBox);
      }
    }
    return boundingBox;
  }

  public int indexOf(final Layer layer) {
    return layers.indexOf(layer);
  }

  @Override
  public int indexOf(final Object o) {
    return layers.indexOf(o);
  }

  @Override
  public boolean isEmpty() {
    return layers.isEmpty();
  }

  @Override
  public boolean isQueryable() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public boolean isQuerySupported() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Iterator<Layer> iterator() {
    // TODO avoid modification
    return layers.iterator();
  }

  @Override
  public int lastIndexOf(final Object o) {
    return layers.lastIndexOf(o);
  }

  @Override
  public ListIterator<Layer> listIterator() {
    // TODO avoid modification
    return layers.listIterator();
  }

  @Override
  public ListIterator<Layer> listIterator(final int index) {
    // TODO avoid modification
    return layers.listIterator(index);
  }

  @Override
  public void refresh() {
    for (final Layer layer : layers) {
      layer.refresh();
    }
  }

  @Override
  public void remove() {
    synchronized (layers) {
      int index = 0;
      for (final Iterator<Layer> iterator = layers.iterator(); iterator.hasNext();) {
        final Layer layer = iterator.next();
        iterator.remove();
        layer.setLayerGroup(null);
        layer.removePropertyChangeListener(this);
        final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
        propertyChangeSupport.fireIndexedPropertyChange("layers", index, layer,
          null);
        layer.remove();
        index++;
      }
      super.remove();
    }
  }

  @Override
  public Layer remove(final int index) {
    synchronized (layers) {
      final Layer layer = layers.remove(index);
      layer.setLayerGroup(null);
      layer.removePropertyChangeListener(this);
      final PropertyChangeSupport propertyChangeSupport = getPropertyChangeSupport();
      propertyChangeSupport.fireIndexedPropertyChange("layers", index, layer,
        null);
      return layer;
    }
  }

  @Override
  public boolean remove(final Object o) {
    synchronized (layers) {
      final int index = layers.indexOf(o);
      if (index < 0) {
        return false;
      } else {
        remove(index);
        return true;
      }
    }
  }

  @Override
  public boolean removeAll(final Collection<?> c) {
    synchronized (layers) {
      final boolean removed = false;
      for (Object layer : c) {
        if (remove(layer)) {
          layer = true;
        }
      }
      return removed;
    }
  }

  @Override
  public boolean retainAll(final Collection<?> c) {
    synchronized (layers) {
      return layers.retainAll(c);
    }
  }

  @Override
  public Layer set(final int index, final Layer element) {
    // TODO events
    return layers.set(index, element);
  }

  @Override
  public int size() {
    return layers.size();
  }

  public void sort() {
    synchronized (layers) {
      Collections.sort(layers);
    }
  }

  @Override
  public List<Layer> subList(final int fromIndex, final int toIndex) {
    return layers.subList(fromIndex, toIndex);
  }

  @Override
  public Object[] toArray() {
    return layers.toArray();
  }

  @Override
  public <T> T[] toArray(final T[] a) {
    return layers.toArray(a);
  }
}
