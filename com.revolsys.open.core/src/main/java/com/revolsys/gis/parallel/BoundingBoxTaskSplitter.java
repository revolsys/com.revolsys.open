package com.revolsys.gis.parallel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.parallel.process.AbstractProcess;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public abstract class BoundingBoxTaskSplitter extends AbstractProcess {
  private final Logger log = LoggerFactory.getLogger(getClass());

  private BoundingBox boundingBox;

  private int numX = 10;

  private int numY = 10;

  private boolean logScriptInfo;

  private Geometry boundary;

  private PreparedGeometry preparedBoundary;

  public abstract void execute(BoundingBox cellBoundingBox);

  public Geometry getBoundary() {
    return boundary;
  }

  public BoundingBox getBoundingBox() {
    return boundingBox;
  }

  public int getNumX() {
    return numX;
  }

  public int getNumY() {
    return numY;
  }

  public boolean isLogScriptInfo() {
    return logScriptInfo;
  }

  protected void postRun() {
  }

  protected void preRun() {
    if (boundingBox != null) {
      if (boundary != null) {
        preparedBoundary = PreparedGeometryFactory.prepare(boundary);
      }
    }
  }

  @Override
  public void run() {
    preRun();
    try {
      if (boundingBox != null) {
        final GeometryFactory geometryFactory = boundingBox.getGeometryFactory();
        final double xInc = boundingBox.getWidth() / numX;
        final double yInc = boundingBox.getHeight() / numY;
        double y = boundingBox.getMinY();
        for (int j = 0; j < numX; j++) {
          double x = boundingBox.getMinX();
          for (int i = 0; i < numX; i++) {
            final BoundingBox cellBoundingBox = new BoundingBox(
              geometryFactory, x, y, x + xInc, y + yInc);
            if (preparedBoundary == null
              || preparedBoundary.intersects(cellBoundingBox.toPolygon(50))) {
              if (logScriptInfo) {
                log.info("Processing bounding box "
                  + cellBoundingBox.toPolygon(1));
              }
              execute(cellBoundingBox);
            }
            x += xInc;
          }
          y += yInc;
        }
      }
    } finally {
      postRun();
    }
  }

  public void setBoundary(final Geometry boundary) {
    this.boundary = boundary;
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    this.boundingBox = boundingBox;
  }

  public void setLogScriptInfo(final boolean logScriptInfo) {
    this.logScriptInfo = logScriptInfo;
  }

  public void setNumX(final int numX) {
    this.numX = numX;
  }

  public void setNumY(final int numY) {
    this.numY = numY;
  }

}
