package com.revolsys.record.io.format.esri.rest.map;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.revolsys.collection.Parent;
import com.revolsys.collection.map.MapEx;
import com.revolsys.io.PathName;
import com.revolsys.record.io.format.esri.rest.ArcGisRestCatalog;
import com.revolsys.record.io.format.esri.rest.ArcGisRestService;
import com.revolsys.record.io.format.esri.rest.CatalogElement;
import com.revolsys.util.Debug;
import com.revolsys.util.Property;

public abstract class ArcGisRestAbstractLayerService extends ArcGisRestService
  implements Parent<CatalogElement> {

  private List<CatalogElement> children = Collections.emptyList();

  private Map<String, LayerDescription> rootLayersByName = Collections.emptyMap();

  private List<TableDescription> tables = new ArrayList<>();

  public ArcGisRestAbstractLayerService(final ArcGisRestCatalog catalog, final String servicePath,
    final String type) {
    super(catalog, servicePath, type);
  }

  protected ArcGisRestAbstractLayerService(final String type) {
    super(type);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <C extends CatalogElement> C getChild(final String name) {
    refreshIfNeeded();
    return (C)this.rootLayersByName.get(name);
  }

  @Override
  public List<CatalogElement> getChildren() {
    refreshIfNeeded();
    return this.children;
  }

  @SuppressWarnings("unchecked")
  public <L extends LayerDescription> L getLayer(final PathName pathName) {
    refreshIfNeeded();
    final List<String> elements = pathName.getElements();
    if (!elements.isEmpty()) {
      LayerDescription layer = this.rootLayersByName.get(elements.get(0));
      for (int i = 1; layer != null && i < elements.size(); i++) {
        if (layer instanceof LayerGroupDescription) {
          final LayerGroupDescription layerGroup = (LayerGroupDescription)layer;
          final String childLayerName = elements.get(i);
          layer = layerGroup.getLayer(childLayerName);
        } else {
          return null;
        }

      }
      return (L)layer;
    }
    return null;
  }

  public List<TableDescription> getTables() {
    return this.tables;
  }

  @SuppressWarnings("unchecked")
  protected void initChildren(final MapEx properties, final List<CatalogElement> children,
    final Map<String, LayerDescription> rootLayersByName) {
    this.tables = newList(TableDescription.class, properties, "tables");

    final Map<Integer, LayerDescription> layersById = new HashMap<>();
    final Map<LayerGroupDescription, List<Number>> layerGroups = new HashMap<>();
    for (final MapEx layerProperties : (List<MapEx>)properties.getValue("layers")) {
      final Integer parentLayerId = layerProperties.getInteger("parentLayerId");
      final Integer id = layerProperties.getInteger("id");
      final String name = layerProperties.getString("name");
      LayerDescription layer;
      final List<Number> subLayerIds = (List<Number>)layerProperties.getValue("subLayerIds");
      if (Property.hasValue(subLayerIds)) {
        final LayerGroupDescription layerGroup = new LayerGroupDescription(this, id, name);
        layerGroups.put(layerGroup, subLayerIds);
        layer = layerGroup;
      } else {
        layer = new RecordLayerDescription(this, id, name);
      }
      layersById.put(id, layer);
      if (parentLayerId == -1) {
        children.add(layer);
        rootLayersByName.put(name, layer);
      }
    }
    for (final Entry<LayerGroupDescription, List<Number>> entry : layerGroups.entrySet()) {
      final LayerGroupDescription layerGroup = entry.getKey();
      final List<Number> subLayerIds = entry.getValue();
      final List<LayerDescription> layers = new ArrayList<>();
      for (final Number layerId : subLayerIds) {
        final LayerDescription layer = layersById.get(layerId.intValue());
        if (layer == null) {
          Debug.noOp();
        } else {
          layer.setParent(layerGroup);
          layers.add(layer);
        }
      }
      layerGroup.setLayers(layers);
    }
  }

  @Override
  protected void initialize(final MapEx properties) {
    final List<CatalogElement> children = new ArrayList<>();
    final Map<String, LayerDescription> rootLayersByName = new HashMap<>();

    initChildren(properties, children, rootLayersByName);
    this.children = Collections.unmodifiableList(children);
    this.rootLayersByName = Collections.unmodifiableMap(rootLayersByName);
    super.initialize(properties);
  }
}
