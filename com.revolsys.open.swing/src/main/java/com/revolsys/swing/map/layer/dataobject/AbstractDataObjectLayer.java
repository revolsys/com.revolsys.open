package com.revolsys.swing.map.layer.dataobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.AbstractLayer;
import com.revolsys.swing.map.symbolizer.LineSymbolizer;
import com.revolsys.swing.map.symbolizer.PointSymbolizer;
import com.revolsys.swing.map.symbolizer.PolygonSymbolizer;
import com.revolsys.swing.map.symbolizer.Symbolizer;
import com.vividsolutions.jts.geom.Geometry;

public abstract class AbstractDataObjectLayer extends AbstractLayer implements
  DataObjectLayer {
  private static final DataObjectLayerRenderer RENDERER = new DataObjectLayerRenderer();

  private static List<Symbolizer> createDefaultStyles() {
    final List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
    symbolizers.add(new PointSymbolizer());
    symbolizers.add(new LineSymbolizer());
    symbolizers.add(new PolygonSymbolizer());
    return symbolizers;
  }

  public int setSelectedWithinDistance(final boolean selected,
    final Geometry geometry, final int distance) {
    final List<DataObject> objects = getObjects(geometry, distance);
    if (selected) {
      selectedObjects.addAll(objects);
    } else {
      selectedObjects.removeAll(objects);
    }
    return objects.size();
  }

  private List<Symbolizer> symbolizers = Collections.emptyList();

  private DataObjectMetaData metaData;

  private Set<DataObject> selectedObjects = new LinkedHashSet<DataObject>();

  private Set<DataObject> editingObjects = new LinkedHashSet<DataObject>();

  @Override
  public void setEditingObjects(
    Collection<? extends DataObject> invisibleObjects) {
    this.editingObjects = new LinkedHashSet<DataObject>(invisibleObjects);
  }

  @Override
  public void clearEditingObjects() {
    this.editingObjects.clear();
  }

  public List<DataObject> getSelectedObjects() {
    return new ArrayList<DataObject>(selectedObjects);
  }

  @Override
  public Set<DataObject> getEditingObjects() {
    return editingObjects;
  }

  @Override
  public int getRowCount() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<DataObject> getObjects() {
    throw new UnsupportedOperationException();
  }

  @Override
  public DataObject getObject(int row) {
    throw new UnsupportedOperationException();
  }

  public List<DataObject> getObjects(Geometry geometry, double distance) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void selectObjects(List<DataObject> objects) {
    selectedObjects.addAll(objects);
  }

  public void selectObjects(DataObject... objects) {
    selectObjects(Arrays.asList(objects));
  }

  @Override
  public void deleteObjects(List<DataObject> objects) {
    throw new UnsupportedOperationException();
  }

  public void deleteObjects(DataObject... objects) {
    deleteObjects(Arrays.asList(objects));
  }

  @Override
  public void clearSelection() {
    selectedObjects = new LinkedHashSet<DataObject>();
  }

  public AbstractDataObjectLayer() {
    setSymbolizers(createDefaultStyles());
    setGeometryFactory(GeometryFactory.getFactory(4326));
    setRenderer(RENDERER);
  }

  public AbstractDataObjectLayer(final DataObjectMetaData metaData) {
    this(metaData.getTypeName());
    setMetaData(metaData);
  }

  public AbstractDataObjectLayer(final String name,
    final GeometryFactory geometryFactory) {
    this(name);
    setGeometryFactory(geometryFactory);
  }

  public AbstractDataObjectLayer(final String name) {
    super(name);
    setReadOnly(false);
    setSelectSupported(true);
    setQuerySupported(true);
    setSymbolizers(createDefaultStyles());
    setRenderer(RENDERER);
  }

  public void addSymbolizer(final Symbolizer symbolizer) {
    if (symbolizer != null) {
      symbolizers.add(symbolizer);
    }

  }

  public CoordinateSystem getCoordinateSystem() {
    return getGeometryFactory().getCoordinateSystem();
  }

  public List<DataObject> getDataObjects(final Viewport2D viewport) {
    return getDataObjects(viewport, viewport.getBoundingBox());
  }

  public List<DataObject> getDataObjects(final Viewport2D viewport,
    final BoundingBox boundingBox) {
    return Collections.emptyList();
  }

  public DataObjectMetaData getMetaData() {
    return metaData;
  }

  public List<Symbolizer> getSymbolizers() {
    return symbolizers;
  }

  public List<Symbolizer> getSymbolizers(final DataObject dataObject) {
    return symbolizers;
  }

  protected void setMetaData(final DataObjectMetaData metaData) {
    this.metaData = metaData;
    setGeometryFactory(metaData.getGeometryFactory());
  }

  public void setSymbolizers(final List<Symbolizer> symbolizers) {
    final List<Symbolizer> oldValue = this.symbolizers;
    for (final Symbolizer symbolizer : oldValue) {
      symbolizer.removePropertyChangeListener(this);
    }
    this.symbolizers = new ArrayList<Symbolizer>();
    for (final Symbolizer symbolizer : symbolizers) {
      symbolizer.addPropertyChangeListener(this);
      this.symbolizers.add(symbolizer);
    }
    getPropertyChangeSupport().firePropertyChange("symbolizers", oldValue,
      this.symbolizers);
  }

  public void setSymbolizers(final Symbolizer... symbolizers) {
    setSymbolizers(Arrays.asList(symbolizers));
  }

}
