package com.revolsys.swing.map.old;

import java.util.List;

import javax.swing.JComponent;

import com.revolsys.gis.algorithm.index.quadtree.QuadTree;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.swing.map.layer.Project;

public class HotspotViewport2D extends com.revolsys.swing.map.ComponentViewport2D {

  private ZoomMode zoomMode = FixedScaleZoomMode.METRIC;

  private QuadTree<ViewportHotspot> toolTipIndex = new QuadTree<>();

  public HotspotViewport2D(final Project project, final JComponent component) {
    super(project, component);
  }

  public void addHotSpot(final GeometryFactory geometryFactory, final Point point,
    final String text, final String url) {
    Point coordinate;

    if (!geometryFactory.equals(getGeometryFactory())) {
      final Point newPoint = point.convert(getGeometryFactory());
      coordinate = newPoint.getPoint();
    } else {
      coordinate = point.getPoint();
    }
    this.toolTipIndex.insert(new BoundingBoxDoubleGf(coordinate),
      new ViewportHotspot(coordinate, text, url));
  }

  public ViewportHotspot getHotspot(final int x, final int y) {
    ViewportHotspot hotSpot = null;
    final double[] location = toModelCoordinates(x, y);
    final double[] location1 = toModelCoordinates(x - 8, y - 8);
    final double[] location2 = toModelCoordinates(x + 8, y + 8);
    final Point coordinate = new PointDouble(location[0], location[1]);
    double closestDistance = Double.MAX_VALUE;

    final BoundingBox envelope = new BoundingBoxDoubleGf(2, location1[0], location2[0],
      location1[1], location2[1]);
    final List<ViewportHotspot> results = this.toolTipIndex.query(envelope);
    for (final ViewportHotspot result : results) {
      final Point point = result.getCoordinate();
      final double distance = point.distance(coordinate);
      if (envelope.covers(point)) {
        if (distance < closestDistance) {
          closestDistance = distance;
          hotSpot = result;
        }
      }
    }
    return hotSpot;
  }

  public String getToolTipText(final int x, final int y) {
    final ViewportHotspot hotSpot = getHotspot(x, y);
    if (hotSpot == null) {
      return null;
    } else {
      return hotSpot.getText();
    }
  }

  public ZoomMode getZoomMode() {
    return this.zoomMode;
  }

  @Override
  public BoundingBox setBoundingBox(final BoundingBox boundingBox) {
    final BoundingBox newBoundingBox = this.zoomMode.getBoundingBox(this, boundingBox);
    if (newBoundingBox.equals(this.getBoundingBox())) {
      return this.getBoundingBox();
    } else {
      this.toolTipIndex = new QuadTree<>();
      return super.setBoundingBox(newBoundingBox);
    }
  }

  public void setZoomMode(final ZoomMode zoomMode) {
    this.zoomMode = zoomMode;
  }

}
