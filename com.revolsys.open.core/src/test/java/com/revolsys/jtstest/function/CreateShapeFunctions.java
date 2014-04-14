package com.revolsys.jtstest.function;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

import com.revolsys.jts.awt.FontGlyphReader;
import com.revolsys.jts.geom.Coordinate;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.util.AffineTransformation;
import com.revolsys.jts.geom.util.AffineTransformationFactory;
import com.revolsys.jts.util.GeometricShapeFactory;

public class CreateShapeFunctions {

  private static final int DEFAULT_POINTSIZE = 100;

  public static Geometry ellipse(final Geometry g, final int nPts) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setNumPoints(nPts);
    if (g != null) {
      gsf.setEnvelope(g.getEnvelopeInternal());
    } else {
      gsf.setEnvelope(new Envelope(0, 1, 0, 1));
    }
    return gsf.createCircle();
  }

  public static Geometry ellipseRotate(final Geometry g, final int nPts,
    final double ang) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setNumPoints(nPts);
    gsf.setRotation(ang);
    if (g != null) {
      gsf.setEnvelope(g.getEnvelopeInternal());
    } else {
      gsf.setEnvelope(new Envelope(0, 1, 0, 1));
    }
    return gsf.createCircle();
  }

  private static Geometry fontGlyph(final Geometry g, final String text,
    final Font font) {
    final Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    final GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

    final Geometry textGeom = FontGlyphReader.read(text, font, geomFact);
    final Envelope envText = textGeom.getEnvelopeInternal();

    if (g != null) {
      // transform to baseline
      final Coordinates baseText0 = new Coordinate(envText.getMinX(),
        envText.getMinY());
      final Coordinates baseText1 = new Coordinate(envText.getMaxX(),
        envText.getMinY());
      final Coordinates baseGeom0 = new Coordinate(env.getMinX(), env.getMinY());
      final Coordinates baseGeom1 = new Coordinate(env.getMaxX(), env.getMinY());
      final AffineTransformation trans = AffineTransformationFactory.createFromBaseLines(
        baseText0, baseText1, baseGeom0, baseGeom1);
      return trans.transform(textGeom);
    }
    return textGeom;
  }

  public static Geometry fontGlyphMonospaced(final Geometry g, final String text) {
    return fontGlyph(g, text, new Font(FontGlyphReader.FONT_MONOSPACED,
      Font.PLAIN, DEFAULT_POINTSIZE));
  }

  public static Geometry fontGlyphSanSerif(final Geometry g, final String text) {
    return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SANSERIF,
      Font.PLAIN, DEFAULT_POINTSIZE));
  }

  public static Geometry fontGlyphSerif(final Geometry g, final String text) {
    return fontGlyph(g, text, new Font(FontGlyphReader.FONT_SERIF, Font.PLAIN,
      DEFAULT_POINTSIZE));
  }

  public static Geometry grid(final Geometry g, final int nCells) {
    final Envelope env = FunctionsUtil.getEnvelopeOrDefault(g);
    final GeometryFactory geomFact = FunctionsUtil.getFactoryOrDefault(g);

    final int nCellsOnSideY = (int)Math.sqrt(nCells);
    final int nCellsOnSideX = nCells / nCellsOnSideY;

    // alternate: make square cells, with varying grid width/height
    // double extent = env.minExtent();
    // double nCellsOnSide = Math.max(nCellsOnSideY, nCellsOnSideX);

    final double cellSizeX = env.getWidth() / nCellsOnSideX;
    final double cellSizeY = env.getHeight() / nCellsOnSideY;

    final List geoms = new ArrayList();

    for (int i = 0; i < nCellsOnSideX; i++) {
      for (int j = 0; j < nCellsOnSideY; j++) {
        final double x = env.getMinX() + i * cellSizeX;
        final double y = env.getMinY() + j * cellSizeY;

        final Envelope cellEnv = new Envelope(x, x + cellSizeX, y, y
          + cellSizeY);
        geoms.add(geomFact.toGeometry(cellEnv));
      }
    }
    return geomFact.buildGeometry(geoms);
  }

  public static Geometry squircle(final Geometry g, final int nPts) {
    return supercircle(g, nPts, 4);
  }

  public static Geometry supercircle(final Geometry g, final int nPts,
    final double pow) {
    final GeometricShapeFactory gsf = new GeometricShapeFactory();
    gsf.setNumPoints(nPts);
    if (g != null) {
      gsf.setEnvelope(g.getEnvelopeInternal());
    } else {
      gsf.setEnvelope(new Envelope(0, 1, 0, 1));
    }
    return gsf.createSupercircle(pow);
  }

  public static Geometry supercircle3(final Geometry g, final int nPts) {
    return supercircle(g, nPts, 3);
  }

  public static Geometry supercircle5(final Geometry g, final int nPts) {
    return supercircle(g, nPts, 5);
  }

  public static Geometry supercirclePoint5(final Geometry g, final int nPts) {
    return supercircle(g, nPts, 0.5);
  }
}
