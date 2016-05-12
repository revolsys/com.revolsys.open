package com.revolsys.record.io.format.esri.rest.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.Parent;
import com.revolsys.record.io.format.esri.rest.CatalogElement;

public class LayerGroupDescription extends LayerDescription implements Parent<LayerDescription> {
  private List<LayerDescription> layers = new ArrayList<>();

  private final Map<String, LayerDescription> layersByName = new HashMap<>();

  public LayerGroupDescription(final ArcGisRestAbstractLayerService service, final Integer id,
    final String name) {
    super(service, id, name);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends CatalogElement> C getChild(final String name) {
    refreshIfNeeded();
    return (C)this.layersByName.get(name);
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
    refreshIfNeeded();
    return (L)this.layersByName.get(name);
  }

  public List<LayerDescription> getLayers() {
    return this.layers;
  }

  public void setLayers(final List<LayerDescription> layers) {
    this.layers = layers;
    for (final LayerDescription layer : layers) {
      final String layerName = layer.getName();
      this.layersByName.put(layerName, layer);
    }
  }
}
