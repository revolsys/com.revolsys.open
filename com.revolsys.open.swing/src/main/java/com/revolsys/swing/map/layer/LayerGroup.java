package com.revolsys.swing.map.layer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import javax.swing.JOptionPane;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.data.io.AbstractDataObjectReaderFactory;
import com.revolsys.io.FileUtil;
import com.revolsys.io.PathUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.map.action.AddFileLayerAction;
import com.revolsys.swing.map.layer.dataobject.DataObjectFileLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.raster.AbstractGeoReferencedImageFactory;
import com.revolsys.swing.map.layer.raster.GeoReferencedImageLayer;
import com.revolsys.swing.menu.MenuFactory;
import com.revolsys.swing.tree.TreeItemRunnable;
import com.revolsys.swing.tree.model.ObjectTreeModel;

public class LayerGroup extends AbstractLayer implements List<Layer> {

  public static final MapObjectFactory FACTORY = new InvokeMethodMapObjectFactory(
    "layerGroup", "Layer Group", LayerGroup.class, "create");

  static {
    final MenuFactory menu = ObjectTreeModel.getMenu(LayerGroup.class);
    menu.addGroup(0, "group");
    menu.addMenuItem("group",
      TreeItemRunnable.createAction("Add Group", "folder_add", "addLayerGroup"));
    menu.addMenuItem("group", new AddFileLayerAction());
  }

  public static LayerGroup create(final Map<String, Object> properties) {
    final LayerGroup layerGroup = new LayerGroup();
    layerGroup.loadLayers(properties);
    return layerGroup;
  }

  private static Layer getLayer(LayerGroup group, final String name) {
    for (final String path : PathUtil.getPathElements(PathUtil.getPath(name))) {
      final Layer layer = getLayerByName(group, path);
      if (layer instanceof LayerGroup) {
        group = (LayerGroup)layer;
      } else {
        return null;
      }
    }

    if (group != null) {
      final String layerName = PathUtil.getName(name);

      return getLayerByName(group, layerName);
    }
    return null;
  }

  private static Layer getLayerByName(final LayerGroup group,
    final String layerName) {
    for (final Layer layer : group.getLayers()) {
      if (layer.getName().equals(layerName)) {

        return layer;
      }
    }
    return null;
  }

  private List<Layer> layers = new ArrayList<Layer>();

  public LayerGroup() {
    this(null);
  }

  public LayerGroup(final String name) {
    super(name);
    setType("layerGroup");
    setRenderer(new LayerGroupRenderer(this));
  }

  @Override
  public void add(final int index, final Layer layer) {
    synchronized (layers) {
      if (layer != null && !layers.contains(layer)) {
        layers.add(index, layer);
        layer.setLayerGroup(this);
        fireIndexedPropertyChange("layers", index, null, layer);
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
  public boolean addAll(int index, final Collection<? extends Layer> layers) {
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

  public LayerGroup addLayerGroup() {
    final String name = JOptionPane.showInputDialog(
      SwingUtil.getActiveWindow(), "Enter the name of the new Layer Group.",
      "Add Layer Group", JOptionPane.PLAIN_MESSAGE);
    if (StringUtils.hasText(name)) {
      return addLayerGroup(name);
    } else {
      return null;
    }
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
    firePropertyChange("layers", oldLayers, layers);
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
  public void delete() {
    synchronized (layers) {
      int index = 0;
      for (final Iterator<Layer> iterator = layers.iterator(); iterator.hasNext();) {
        final Layer layer = iterator.next();
        iterator.remove();
        layer.setLayerGroup(null);
        layer.removePropertyChangeListener(this);
        fireIndexedPropertyChange("layers", index, layer, null);
        layer.delete();
        index++;
      }
      super.delete();
      this.layers = null;
    }
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
    return (V)getLayer(this, name);
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

  protected void loadLayer(final File file) {
    final Resource oldResource = SpringUtil.setBaseResource(new FileSystemResource(
      file.getParentFile()));

    try {
      final Map<String, Object> properties = JsonMapIoFactory.toMap(file);
      final Layer layer = MapObjectFactoryRegistry.toObject(properties);
      if (layer != null) {
        add(layer);
      }
    } catch (final Throwable t) {
      LoggerFactory.getLogger(getClass()).error(
        "Cannot load layer from " + file, t);
    } finally {
      SpringUtil.setBaseResource(oldResource);
    }
  }

  @SuppressWarnings("unchecked")
  protected void loadLayers(final Map<String, Object> properties) {
    final List<String> layerFiles = (List<String>)properties.remove("layers");
    setProperties(properties);
    if (layerFiles != null) {
      for (String fileName : layerFiles) {
        if (!fileName.endsWith("rgobject")) {
          fileName += "/rgLayerGroup.rgobject";
        }
        final Resource childResource = SpringUtil.getBaseResource(fileName);
        if (childResource.exists()) {
          final Object object = MapObjectFactoryRegistry.toObject(childResource);
          if (object instanceof Layer) {
            final Layer layer = (Layer)object;
            add(layer);
          } else if (object != null) {
            LoggerFactory.getLogger(LayerGroup.class).error(
              "Unexpected object type " + object.getClass() + " in "
                + childResource);
          }
        } else {
          LoggerFactory.getLogger(LayerGroup.class).error(
            "Cannot find " + childResource);
        }
      }
    }
  }

  public void openFile(final File file) {
    final String extension = FileUtil.getFileNameExtension(file);
    if ("rgobject".equals(extension)) {
      loadLayer(file);
    } else {
      final FileSystemResource resource = new FileSystemResource(file);
      if (AbstractGeoReferencedImageFactory.hasGeoReferencedImageFactory(resource)) {
        final GeoReferencedImageLayer layer = new GeoReferencedImageLayer(
          resource);
        add(layer);
        layer.setEditable(true);
      } else if (AbstractDataObjectReaderFactory.hasDataObjectReaderFactory(resource)) {
        final DataObjectFileLayer layer = new DataObjectFileLayer(resource);
        final GeometryStyleRenderer renderer = layer.getRenderer();
        renderer.setStyle(GeometryStyle.createStyle());
        add(layer);
      }
    }
  }

  public void openFiles(final List<File> files) {
    for (final File file : files) {
      openFile(file);
    }
  }

  @Override
  public void refresh() {
    for (final Layer layer : layers) {
      layer.refresh();
    }
  }

  @Override
  public Layer remove(final int index) {
    synchronized (layers) {
      final Layer layer = layers.remove(index);
      layer.setLayerGroup(null);
      layer.removePropertyChangeListener(this);
      fireIndexedPropertyChange("layers", index, layer, null);
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
  public boolean saveChanges() {
    boolean saved = true;
    for (final Layer layer : this) {
      saved &= layer.saveChanges();
    }
    return saved;
  }

  protected void saveLayerGroup(final File directory) {
    final File file = new File(directory, "rgLayerGroup.rgobject");
    MapObjectFactoryRegistry.write(file, this);
    for (final Layer child : this) {
      final String childName = child.getName();
      final String childFileName = FileUtil.getSafeFileName(childName);
      if (child instanceof LayerGroup) {
        final LayerGroup childLayerGroup = (LayerGroup)child;
        final File childDirectory = new File(directory, childFileName);
        childDirectory.mkdirs();
        childLayerGroup.saveLayerGroup(childDirectory);
      } else {
        final File childFile = new File(directory, childFileName + ".rgobject");
        MapObjectFactoryRegistry.write(childFile, child);
      }
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

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = super.toMap();
    final List<String> layerFiles = new ArrayList<String>();
    final List<Layer> layers = getLayers();
    for (final Layer layer : layers) {
      final String layerName = layer.getName();
      String layerFileName = FileUtil.getSafeFileName(layerName);
      if (layer instanceof LayerGroup) {
      } else {
        layerFileName += ".rgobject";
      }
      layerFiles.add(layerFileName);

    }
    map.put("layers", layerFiles);
    return map;
  }

}
