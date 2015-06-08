package com.revolsys.jts.testold.algorithm;

import java.util.Random;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.PointDouble;

public class PerturbedGridPolygonBuilder {
  private final GeometryFactory geomFactory;

  private final double gridWidth = 1000;

  private int numLines = 10;

  private double lineWidth = 20;

  private long seed = 0;

  private Random rand;

  private Geometry grid;

  public PerturbedGridPolygonBuilder(final GeometryFactory geomFactory) {
    this.geomFactory = geomFactory;
    this.seed = System.currentTimeMillis();
  }

  private Geometry buildGrid() {
    final LineString[] lines = new LineString[this.numLines * 2];
    int index = 0;

    for (int i = 0; i < this.numLines; i++) {
      final Point p0 = new PointDouble(getRandOrdinate(), 0, Point.NULL_ORDINATE);
      final Point p1 = new PointDouble(getRandOrdinate(), this.gridWidth, Point.NULL_ORDINATE);
      final LineString line = this.geomFactory.lineString(new Point[] {
        p0, p1
      });
      lines[index++] = line;
    }

    for (int i = 0; i < this.numLines; i++) {
      final Point p0 = new PointDouble((double)0, getRandOrdinate(), Point.NULL_ORDINATE);
      final Point p1 = new PointDouble(this.gridWidth, getRandOrdinate(), Point.NULL_ORDINATE);
      final LineString line = this.geomFactory.lineString(new Point[] {
        p0, p1
      });
      lines[index++] = line;
    }

    final MultiLineString ml = this.geomFactory.multiLineString(lines);
    final Geometry grid = ml.buffer(this.lineWidth);
    // System.out.println(grid);
    return grid;

  }

  public Geometry getGeometry() {
    if (this.grid == null) {
      this.grid = buildGrid();
    }
    return this.grid;
  }

  private double getRand() {
    if (this.rand == null) {
      // System.out.println("Seed = " + this.seed);
      this.rand = new Random(this.seed);
    }
    return this.rand.nextDouble();
  }

  private double getRandOrdinate() {
    final double randNum = getRand();
    final double ord = this.geomFactory.makePrecise(0, randNum * this.gridWidth);
    return ord;
  }

  public void setLineWidth(final double lineWidth) {
    this.lineWidth = lineWidth;
  }

  public void setNumLines(final int numLines) {
    this.numLines = numLines;
  }

  public void setSeed(final long seed) {
    this.seed = seed;
  }

}
