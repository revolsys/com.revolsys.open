package com.revolsys.jtstest.testbuilder.ui.render;

import java.awt.Graphics2D;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jtstest.testbuilder.Viewport;
import com.revolsys.jtstest.testbuilder.model.GeometryContainer;
import com.revolsys.jtstest.testbuilder.model.Layer;
import com.revolsys.jtstest.testbuilder.ui.style.Style;

public class LayerRenderer implements Renderer {
  private final Layer layer;

  private final GeometryContainer geomCont;

  private final Viewport viewport;

  private boolean isCancelled = false;

  public LayerRenderer(final Layer layer, final GeometryContainer geomCont,
    final Viewport viewport) {
    this.layer = layer;
    this.geomCont = geomCont;
    this.viewport = viewport;
  }

  public LayerRenderer(final Layer layer, final Viewport viewport) {
    this(layer, layer.getSource(), viewport);
  }

  @Override
  public void cancel() {
    isCancelled = true;
  }

  private Geometry getGeometry() {
    if (geomCont == null) {
      return null;
    }
    final Geometry geom = geomCont.getGeometry();
    return geom;
  }

  @Override
  public void render(final Graphics2D g) {
    if (!layer.isEnabled()) {
      return;
    }

    try {
      final Geometry geom = getGeometry();
      if (geom == null) {
        return;
      }

      render(g, viewport, geom, layer.getStyles());

    } catch (final Exception ex) {
      System.out.println(ex);
      // not much we can do about it - just carry on
    }
  }

  private void render(final Graphics2D g, final Viewport viewport,
    final Geometry geometry, final Style style) throws Exception {
    // cull non-visible geometries
    // for maximum rendering speed this needs to be checked for each component
    if (!viewport.intersectsInModel(geometry.getBoundingBox())) {
      return;
    }

    if (geometry instanceof GeometryCollection) {
      renderGeometryCollection(g, viewport, (GeometryCollection)geometry, style);
      return;
    }

    style.paint(geometry, viewport, g);
  }

  private void renderGeometryCollection(final Graphics2D g,
    final Viewport viewport, final GeometryCollection gc, final Style style)
    throws Exception {
    /**
     * Render each element separately.
     * Otherwise it is not possible to render both filled and non-filled
     * (1D) elements correctly.
     * This also allows cancellation.
     */
    for (int i = 0; i < gc.getGeometryCount(); i++) {
      render(g, viewport, gc.getGeometry(i), style);
      if (isCancelled) {
        return;
      }
    }
  }

}
