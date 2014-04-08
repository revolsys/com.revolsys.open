package com.revolsys.swing.map.layer.dataobject.renderer;

import java.awt.Graphics2D;
import java.util.Collections;
import java.util.Map;

import javax.swing.Icon;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.jts.geom.TopologyException;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.LayerDataObject;
import com.revolsys.util.ExceptionUtil;

/**
 * For each object render using the first renderer that matches the filter.
 */
public class FilterMultipleRenderer extends AbstractMultipleRenderer {

  private static final Icon ICON = SilkIconLoader.getIcon("style_filter");

  public FilterMultipleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent) {
    this(layer, parent, Collections.<String, Object> emptyMap());
  }

  public FilterMultipleRenderer(final AbstractDataObjectLayer layer,
    final LayerRenderer<?> parent, final Map<String, Object> style) {
    super("filterStyle", layer, parent, style);
    setIcon(ICON);
  }

  @Override
  public void renderRecord(final Viewport2D viewport,
    final Graphics2D graphics, final BoundingBox visibleArea,
    final AbstractDataObjectLayer layer, final LayerDataObject record) {
    if (isVisible(record)) {
      final double scale = viewport.getScale();
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isFilterAccept(record)) {
          if (renderer.isVisible(record) && !layer.isHidden(record)) {
            if (renderer.isVisible(scale)) {
              try {
                renderer.renderRecord(viewport, graphics, visibleArea, layer,
                  record);
              } catch (final TopologyException e) {
              } catch (final Throwable e) {
                ExceptionUtil.log(
                  getClass(),
                  "Unabled to render " + layer.getName() + " #"
                    + record.getIdString(), e);
              }
            }
          }
          // Only render using the first match
          return;
        }
      }
    }
  }

  @Override
  public void renderSelectedRecord(final Viewport2D viewport,
    final Graphics2D graphics, final AbstractDataObjectLayer layer,
    final LayerDataObject record) {
    if (isVisible(record)) {
      final double scale = viewport.getScale();
      for (final AbstractDataObjectLayerRenderer renderer : getRenderers()) {
        if (renderer.isFilterAccept(record)) {
          if (renderer.isVisible(record)) {
            if (renderer.isVisible(scale)) {
              try {
                renderer.renderSelectedRecord(viewport, graphics, layer, record);
              } catch (final Throwable e) {
                ExceptionUtil.log(
                  getClass(),
                  "Unabled to render " + layer.getName() + " #"
                    + record.getIdString(), e);
              }
            }
          }
          // Only render using the first match
          return;
        }
      }
    }
  }
}
