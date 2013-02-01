package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public class ScaleMultipleRenderer extends AbstractDataObjectLayerRenderer {

  private final List<ScaleAndRenderer> scaleRenderers = new ArrayList<ScaleAndRenderer>();

  private double lastScale = 0;

  private AbstractDataObjectLayerRenderer renderer;

  @SuppressWarnings("unchecked")
  public ScaleMultipleRenderer(Map<String, Object> defaults,
    final Map<String, Object> style) {
    super(defaults, style);
    List<Map<String, Object>> scales = (List<Map<String, Object>>)style.get("scales");
    for (Map<String, Object> scaleStyle : scales) {
      ScaleAndRenderer scaleAndRenderer = new ScaleAndRenderer(getDefaults(),
        scaleStyle);
      scaleRenderers.add(scaleAndRenderer);
    }
  }

  private AbstractDataObjectLayerRenderer getRenderer(final double scale) {
    lastScale = scale;
    for (final ScaleAndRenderer scaleAndRenderer : scaleRenderers) {
      if (scaleAndRenderer.isVisible(scale)) {
        System.out.println(scaleAndRenderer);
        return scaleAndRenderer.getRenderer();
      }
    }
    return null;
  }

  @Override
  public void render(final Viewport2D viewport, final Graphics2D graphics,
    final DataObjectLayer layer) {
    final double scale = viewport.getScale();
    if (layer.isVisible(scale)) {
      if (scale != lastScale) {
        renderer = getRenderer(scale);
      }
      if (renderer != null) {
        renderer.render(viewport, graphics, layer);
      }
    }
  }

  @Override
  public Map<String, Object> toMap() {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "scaleStyle");
    Map<String, Object> defaults = getDefaults();
    if (!defaults.isEmpty()) {
      map.put("defaults", defaults);
    }
    if (!scaleRenderers.isEmpty()) {
      map.put("scales"  , scaleRenderers);
    }
    return map;
  }
}
