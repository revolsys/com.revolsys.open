package com.revolsys.swing.map.layer.record.style.marker;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.List;

import javax.measure.Quantity;
import javax.measure.quantity.Length;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.jeometry.common.function.BiFunctionDouble;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.ImageViewport;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;

public class GeometryMarker extends AbstractMarker {
  private static final MarkerLibrary LIBRARY = MarkerLibrary.newLibrary("shapes", "Shapes");

  static {
    addMarker("arrow", GeometryMarker::arrow);
    addMarker("cross", GeometryMarker::cross);
    addMarker("crossLine", GeometryMarker::crossLine);
    addMarker("diamond", GeometryMarker::diamond);
    addMarker("solidArrow", GeometryMarker::solidArrow);
    addMarker("star", GeometryMarker::star);
    addMarker("triangle", GeometryMarker::triangle);
    addMarker("x", GeometryMarker::x);
    addMarker("xLine", GeometryMarker::xLine);
    LIBRARY.addSymbol(new RectangleMarker());
    LIBRARY.addSymbol(new RectangleMarker("square"));
    LIBRARY.addSymbol(new EllipseMarker());
    LIBRARY.addSymbol(new EllipseMarker("circle"));
  }

  public static GeometryFactory GEOMETRY_FACTORY = GeometryFactory.DEFAULT_2D;

  private static void addMarker(final String name,
    final BiFunctionDouble<Geometry> newMarkerFunction) {
    final GeometryMarker marker = new GeometryMarker(name, newMarkerFunction);
    LIBRARY.addSymbol(marker);
  }

  /**
   * Get an arrow shape pointing right for the size of the graphic.
   *
   * @return The shape.
   */
  public static Geometry arrow(final double width, final double height) {
    return GEOMETRY_FACTORY.lineString(2, //
      0.0, height, //
      width, height * .5, //
      0, 0);
  }

  public static Geometry cross(final double width, final double height) {
    return GEOMETRY_FACTORY.polygon(GEOMETRY_FACTORY.linearRing(2 //
      , width / 3, height //
      , width * 2 / 3, height //
      , width * 2 / 3, height * 2 / 3 //
      , width, height * 2 / 3 //
      , width, height / 3 //
      , width * 2 / 3, height / 3 //
      , width * 2 / 3, 0 //
      , width / 3, 0 //
      , width / 3, height / 3 //
      , 0, height / 3 //
      , 0, height * 2 / 3 //
      , width / 3, height * 2 / 3 //
      , width / 3, height //
    ));
  }

  public static Geometry crossLine(final double width, final double height) {
    return GEOMETRY_FACTORY.lineal(//
      GEOMETRY_FACTORY.lineString(2, width / 2, height, width / 2, 0), //
      GEOMETRY_FACTORY.lineString(2, width, height / 2, 0, height / 2) //
    );
  }

  public static Geometry diamond(final double width, final double height) {
    return GEOMETRY_FACTORY.polygon(GEOMETRY_FACTORY.linearRing(2//
      , width / 2, 0//
      , width, height / 2//
      , width / 2, height//
      , 0, height / 2//
      , width / 2, 0//
    ));
  }

  public static List<Marker> getMarkers() {
    return LIBRARY.getSymbols();
  }

  public static void init() {
  }

  /**
   * Get a solid arrow shape pointing right for the size of the graphic.
   *
   * @return The shape.
   */
  public static Geometry solidArrow(final double width, final double height) {
    return GEOMETRY_FACTORY.polygon(GEOMETRY_FACTORY.linearRing(2 //
      , 0.0, 0 //
      , width, height * .5 //
      , 0, height //
      , 0, 0 //
    ));
  }

  public static Geometry star(final double width, final double height) {
    return GEOMETRY_FACTORY.polygon(GEOMETRY_FACTORY.linearRing(2 //
      , width / 2, 0 //
      , width * 0.64, height / 3 //
      , width, height / 3 //
      , width * .7, height * .57 //
      , width * .8, height * .9 //
      , width / 2, height * .683 //
      , width * .2, height * .9 //
      , width * .3, height * .57 //
      , 0, height / 3 //
      , width * 0.36, height / 3//
      , width / 2, 0 //
    ));
  }

  public static Geometry triangle(final double width, final double height) {
    return GEOMETRY_FACTORY.polygon(GEOMETRY_FACTORY.linearRing(2 //
      , 0.0, height //
      , width / 2, 0 //
      , width, height //
      , 0, height //
    ));
  }

  /**
   * Get an X shape for the height of the graphic.
   *
   * @return The shape.
   */
  public static Geometry x(final double width, final double height) {
    return GEOMETRY_FACTORY.polygon(GEOMETRY_FACTORY.linearRing(2 //
      , width * .25, height //
      , width * .5, height * .75 //
      , width * .75, height //
      , width, height * .75 //
      , width * .75, height * .5 //
      , width, height * .25 //
      , width * .75, 0 //
      , width * .5, height * .25 //
      , width * .25, 0 //
      , 0, height * .25 //
      , width * .25, height * .5 //
      , 0, height * .75//
      , width * .25, height //
    ));
  }

  public static Geometry xLine(final double width, final double height) {
    return GEOMETRY_FACTORY.lineal(//
      GEOMETRY_FACTORY.lineString(2, 0.0, 0, width, height), //
      GEOMETRY_FACTORY.lineString(2, 0.0, width, height, 0)//
    );
  }

  private final BiFunctionDouble<Geometry> newMarkerFunction;

  public GeometryMarker(final BiFunctionDouble<Geometry> newMarkerFunction) {
    this.newMarkerFunction = newMarkerFunction;
  }

  public GeometryMarker(final String name, final BiFunctionDouble<Geometry> newMarkerFunction) {
    super(name);
    this.newMarkerFunction = newMarkerFunction;
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof GeometryMarker) {
      final GeometryMarker marker = (GeometryMarker)object;
      return getName().equals(marker.getName());
    } else {
      return false;
    }
  }

  @Override
  public String getTypeName() {
    return "geometryMarker";
  }

  @Override
  public boolean isUseMarkerName() {
    return true;
  }

  @Override
  public Icon newIcon(final MarkerStyle style) {
    final Geometry geometry = newMarker(15, 15);

    try (
      final ImageViewport viewport = new ImageViewport(16, 16)) {
      final Graphics2DViewRenderer view = viewport.newViewRenderer();
      final Graphics2D graphics = view.getGraphics();
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      if (style.setMarkerFillStyle(view, graphics)) {
        view.fillMarker(geometry);
      }
      if (style.setMarkerLineStyle(view, graphics)) {
        view.drawMarker(geometry);
      }
      graphics.dispose();
      return new ImageIcon(viewport.getImage());
    }
  }

  public Geometry newMarker(final double width, final double height) {
    return this.newMarkerFunction.accept(width, height);
  }

  @Override
  public void render(final Graphics2DViewRenderer view, final Graphics2D graphics,
    final MarkerStyle style, final double modelX, final double modelY, double orientation) {
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

    try (
      BaseCloseable closable = view.useViewCoordinates()) {
      final Quantity<Length> markerWidth = style.getMarkerWidth();
      final double mapWidth = view.toDisplayValue(markerWidth);
      final Quantity<Length> markerHeight = style.getMarkerHeight();
      final double mapHeight = view.toDisplayValue(markerHeight);
      final String orientationType = style.getMarkerOrientationType();
      if ("none".equals(orientationType)) {
        orientation = 0;
      }

      view.translateMarker(style, modelX, modelY, mapWidth, mapHeight, orientation);

      final Geometry geometry = newMarker(mapWidth, mapHeight);

      if (style.setMarkerFillStyle(view, graphics)) {
        view.fillMarker(geometry);
      }
      if (style.setMarkerLineStyle(view, graphics)) {
        view.drawMarker(geometry);
      }
    }
  }

}
