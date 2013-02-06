package com.revolsys.swing.map.layer.dataobject.renderer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public abstract class AbstractMultipleRenderer extends
  AbstractDataObjectLayerRenderer {

  private List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();

  public AbstractMultipleRenderer(final String type, final DataObjectLayer layer) {
    super(type, layer);
  }

  public AbstractMultipleRenderer(final String type,
    final DataObjectLayer layer, final LayerRenderer<?> parent) {
    super(type, layer, parent);
  }

  public AbstractMultipleRenderer(final String type,
    final DataObjectLayer layer, final LayerRenderer<?> parent,
    final Map<String, Object> style) {
    super(type, layer, parent, style);
    @SuppressWarnings("unchecked")
    final List<Map<String, Object>> styles = (List<Map<String, Object>>)style.get("styles");
    List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
     for (final Map<String, Object> childStyle : styles) {
      final AbstractDataObjectLayerRenderer renderer = AbstractDataObjectLayerRenderer.getRenderer(
        layer, this, childStyle);
      renderers.add(renderer);
    }
     setRenderers(renderers);
  }

  public int addRenderer(final AbstractDataObjectLayerRenderer renderer) {
    if (renderer == null) {
      return -1;
    } else {
      renderers.add(renderer);
      return renderers.size() - 1;
    }

  }

  public int addRenderer(final int index,
    final AbstractDataObjectLayerRenderer renderer) {
    renderers.add(index, renderer);
    return index;
  }

  public List<AbstractDataObjectLayerRenderer> getRenderers() {
    return renderers;
  }

  public void setRenderers(
    final List<? extends AbstractDataObjectLayerRenderer> renderers) {
    this.renderers = new ArrayList<AbstractDataObjectLayerRenderer>(renderers);
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "multipleStyle");
    final Map<String, Object> defaults = getDefaults();
    if (!defaults.isEmpty()) {
      map.put("defaults", defaults);
    }
    if (!renderers.isEmpty()) {
      map.put("styles", renderers);
    }
    return map;
  }

  public void removeRenderer(AbstractDataObjectLayerRenderer renderer) {
   renderers.remove(renderer);
  }

}
