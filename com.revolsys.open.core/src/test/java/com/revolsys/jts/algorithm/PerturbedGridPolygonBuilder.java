package com.revolsys.jts.algorithm;

import java.util.Random;

import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.MultiLineString;

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
      final Coordinate p0 = new Coordinate(getRandOrdinate(), 0);
      final Coordinate p1 = new Coordinate(getRandOrdinate(), this.gridWidth);
      final LineString line = this.geomFactory.createLineString(new Coordinate[] {
        p0, p1
      });
      lines[index++] = line;
    }

    for (int i = 0; i < this.numLines; i++) {
      final Coordinate p0 = new Coordinate(0, getRandOrdinate());
      final Coordinate p1 = new Coordinate(this.gridWidth, getRandOrdinate());
      final LineString line = this.geomFactory.createLineString(new Coordinate[] {
        p0, p1
      });
      lines[index++] = line;
    }

    final MultiLineString ml = this.geomFactory.createMultiLineString(lines);
    final Geometry grid = ml.buffer(this.lineWidth);
    System.out.println(grid);
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
      System.out.println("Seed = " + this.seed);
      this.rand = new Random(this.seed);
    }
    return this.rand.nextDouble();
  }

  private double getRandOrdinate() {
    final double randNum = getRand();
    final double ord = this.geomFactory.getPrecisionModel().makePrecise(
      randNum * this.gridWidth);
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
