package com.revolsys.jtstest.function;

import java.awt.Graphics2D;
import java.util.List;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jtstest.testbuilder.AppConstants;
import com.revolsys.jtstest.testbuilder.GeometryEditPanel;
import com.revolsys.jtstest.testbuilder.JTSTestBuilder;
import com.revolsys.jtstest.testbuilder.JTSTestBuilderFrame;
import com.revolsys.jtstest.testbuilder.ui.render.GeometryPainter;

public class FunctionsUtil {

  public static final BoundingBox DEFAULT_ENVELOPE = new Envelope(0, 0, 100, 100);

  public static Geometry buildGeometry(final List geoms,
    final Geometry parentGeom) {
    if (geoms.size() <= 0) {
      return null;
    }
    if (geoms.size() == 1) {
      return (Geometry)geoms.get(0);
    }
    // if parent was a GC, ensure returning a GC
    if (parentGeom.getGeometryType().equals("GeometryCollection")) {
      return parentGeom.getGeometryFactory().createGeometryCollection(
        GeometryFactory.toGeometryArray(geoms));
    }
    // otherwise return MultiGeom
    return parentGeom.getGeometryFactory().buildGeometry(geoms);
  }

  public static BoundingBox getEnvelopeOrDefault(final Geometry g) {
    if (g == null) {
      return DEFAULT_ENVELOPE;
    }
    return g.getBoundingBox();
  }

  public static GeometryFactory getFactoryOrDefault(final Geometry g) {
    if (g == null) {
      return JTSTestBuilder.getGeometryFactory();
    }
    return g.getGeometryFactory();
  }

  public static void showIndicator(final Geometry geom) {
    final GeometryEditPanel panel = JTSTestBuilderFrame.instance()
      .getTestCasePanel()
      .getGeometryEditPanel();
    final Graphics2D gr = (Graphics2D)panel.getGraphics();
    GeometryPainter.paint(geom, panel.getViewport(), gr,
      AppConstants.INDICATOR_LINE_CLR, AppConstants.INDICATOR_FILL_CLR);
  }

}
