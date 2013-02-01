package com.revolsys.swing.map.layer.dataobject.renderer;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScaleAndRenderer {

  private long minimumScale = 0;

  private long maximumScale = Long.MAX_VALUE;

  private AbstractDataObjectLayerRenderer renderer;

  public ScaleAndRenderer(long minimumScale, long maximumScale,
    AbstractDataObjectLayerRenderer renderer) {
    this.minimumScale = minimumScale;
    this.maximumScale = maximumScale;
    this.renderer = renderer;
  }

  public ScaleAndRenderer(Map<String, Object> defaults,
    Map<String, Object> scaleStyle) {
    Map<String, Object> style = new HashMap<String, Object>(defaults);
    style.putAll(scaleStyle);
    Number minimumScale = (Number)style.remove("minimumScale");
    if (minimumScale != null) {
      this.minimumScale = minimumScale.longValue();
    }
    Number maximumScale = (Number)style.remove("maximumScale");
    if (maximumScale != null) {
      this.maximumScale = maximumScale.longValue();
    }
    this.renderer = AbstractDataObjectLayerRenderer.getRenderer(defaults, style);
  }

  public long getMinimumScale() {
    return minimumScale;
  }

  public long getMaximumScale() {
    return maximumScale;
  }

  public AbstractDataObjectLayerRenderer getRenderer() {
    return renderer;
  }

  public void setMinimumScale(final long minimumScale) {
    this.minimumScale = minimumScale;
  }

  public void setMaximumScale(final long maximumScale) {
    this.maximumScale = maximumScale;
  }

  public void setRenderer(final AbstractDataObjectLayerRenderer renderer) {
    this.renderer = renderer;
  }

  public boolean isVisible(double scale) {
    if (scale >= minimumScale) {
      if (scale <= maximumScale) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    Map<String,Object> map = new LinkedHashMap<String, Object>();
    map.put("minimumScale", minimumScale);
    map.put("maximumScale", maximumScale);
    if (renderer != null) {
      map.putAll(renderer.toMap());
    }
    return map.toString();
  }
}
