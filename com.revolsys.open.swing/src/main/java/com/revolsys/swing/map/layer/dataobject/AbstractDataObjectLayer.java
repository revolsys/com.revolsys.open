package com.revolsys.swing.map.layer.dataobject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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

public abstract class AbstractDataObjectLayer extends AbstractLayer {
  private static final DataObjectLayerRenderer RENDERER = new DataObjectLayerRenderer();

  private static List<Symbolizer> createDefaultStyles() {
    final List<Symbolizer> symbolizers = new ArrayList<Symbolizer>();
    symbolizers.add(new PointSymbolizer());
    symbolizers.add(new LineSymbolizer());
    symbolizers.add(new PolygonSymbolizer());
    return symbolizers;
  }

  private List<Symbolizer> symbolizers = Collections.emptyList();

  private DataObjectMetaData metaData;

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
