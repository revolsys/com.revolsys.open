package com.revolsys.swing.map.layer.dataobject.renderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;

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
    final List<AbstractDataObjectLayerRenderer> renderers = new ArrayList<AbstractDataObjectLayerRenderer>();
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
      this.renderers.add(renderer);
      return this.renderers.size() - 1;
    }

  }

  public int addRenderer(final int index,
    final AbstractDataObjectLayerRenderer renderer) {
    this.renderers.add(index, renderer);
    return index;
  }

  public List<AbstractDataObjectLayerRenderer> getRenderers() {
    return this.renderers;
  }

  @Override
  public boolean isVisible(final LayerDataObject object) {
    if (super.isVisible() && super.isVisible(object)) {
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isVisible(object)) {
          return true;
        }
      }
    }
    return false;
  }

  public void removeRenderer(final AbstractDataObjectLayerRenderer renderer) {
    this.renderers.remove(renderer);
  }

  public void setRenderers(
    final List<? extends AbstractDataObjectLayerRenderer> renderers) {
    this.renderers = new ArrayList<AbstractDataObjectLayerRenderer>(renderers);
  }

  @Override
  public Map<String, Object> toMap(final Map<String, Object> defaults) {
    final Map<String, Object> map = super.toMap(defaults);
    final Map<String, Object> allDefaults = getAllDefaults();
    if (!this.renderers.isEmpty()) {
      final List<Map<String, Object>> rendererMaps = new ArrayList<Map<String, Object>>();
      for (final AbstractDataObjectLayerRenderer renderer : this.renderers) {
        rendererMaps.add(renderer.toMap(allDefaults));
      }
      MapSerializerUtil.add(map, "styles", rendererMaps);
    }
    return map;
  }

}
