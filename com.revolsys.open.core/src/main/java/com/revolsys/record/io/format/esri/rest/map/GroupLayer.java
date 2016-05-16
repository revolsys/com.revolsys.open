package com.revolsys.record.io.format.esri.rest.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.revolsys.collection.Parent;
import com.revolsys.collection.list.Lists;
import com.revolsys.collection.map.MapEx;
import com.revolsys.record.io.format.esri.rest.CatalogElement;

public class GroupLayer extends LayerDescription implements Parent<LayerDescription> {
  private List<LayerDescription> layers = new ArrayList<>();

  private Map<String, LayerDescription> layersByName = new HashMap<>();

  private List<Number> subLayerIds = Collections.emptyList();

  public GroupLayer(final ArcGisRestAbstractLayerService service, final MapEx properties) {
    super(service);
    initialize(properties);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends CatalogElement> C getChild(final String name) {
    if (name == null) {
      return null;
    } else {
      refreshIfNeeded();
      return (C)this.layersByName.get(name.toLowerCase());
    }
  }

  @Override
  public List<LayerDescription> getChildren() {
    return getLayers();
  }

  @Override
  public String getIconName() {
    return "folder:table";
  }

  @SuppressWarnings("unchecked")
  public <L extends LayerDescription> L getLayer(final String name) {
    return (L)getChild(name);
  }

  public List<LayerDescription> getLayers() {
    return this.layers;
  }

  public List<Number> getSubLayerIds() {
    return this.subLayerIds;
  }

  @Override
  protected void initialize(final MapEx properties) {
    super.initialize(properties);
    final ArcGisRestAbstractLayerService service = getService();
    final Map<String, LayerDescription> layersByName = new TreeMap<>();
    final List<MapEx> layerDefinitions = properties.getValue("subLayers", Collections.emptyList());
    for (final MapEx layerProperties : layerDefinitions) {
      service.addLayer(this, layersByName, layerProperties);
    }
    this.layers = Lists.toArray(layersByName.values());
    this.layersByName = layersByName;
  }

  public void setSubLayerIds(final List<Number> subLayerIds) {
    this.subLayerIds = subLayerIds;
  }
}
