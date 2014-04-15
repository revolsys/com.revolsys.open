package com.revolsys.jtstest.testbuilder.ui;

import java.util.Iterator;
import java.util.List;

import com.revolsys.jts.algorithm.CGAlgorithms;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.GeometryCollection;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.LinearRing;
import com.revolsys.jts.geom.Point;
import com.revolsys.jtstest.testbuilder.geom.ComponentLocater;
import com.revolsys.jtstest.testbuilder.geom.FacetLocater;
import com.revolsys.jtstest.testbuilder.geom.GeometryLocation;
import com.revolsys.jtstest.testbuilder.geom.VertexLocater;
import com.revolsys.jtstest.testbuilder.model.Layer;
import com.revolsys.jtstest.testbuilder.model.LayerList;

public class GeometryLocationsWriter {
  private static final int MAX_ITEMS_TO_DISPLAY = 10;

  public static String writeLocation(final LayerList layers,
    final Coordinates pt, final double tolerance) {
    final GeometryLocationsWriter writer = new GeometryLocationsWriter();
    return writer.writeLocationString(layers, pt, tolerance);
  }

  private boolean isHtmlFormatted = true;

  private String eol = null;

  private String highlightStart = null;

  private String highlightEnd = null;

  private String documentStart = null;

  private String documentEnd = null;

  public GeometryLocationsWriter() {
    setHtml(true);
  }

  public String OLDwriteLocation(final Geometry geom, final Coordinates p,
    final double tolerance) {
    final VertexLocater locater = new VertexLocater(geom);
    final List locs = locater.getLocations(p, tolerance);

    if (locs.size() <= 0) {
      return null;
    }

    final StringBuffer buf = new StringBuffer();
    boolean isFirst = true;
    for (final Iterator i = locs.iterator(); i.hasNext();) {
      final VertexLocater.Location vertLoc = (VertexLocater.Location)i.next();
      final int index = vertLoc.getIndices()[0];
      final Coordinates pt = vertLoc.getCoordinate();
      if (!isFirst) {
        buf.append(eol + "--");
      }
      isFirst = false;
      final String locStr = "[" + index + "]: " + pt.getX() + ", " + pt.getY();
      buf.append(locStr);
    }

    return buf.toString();
  }

  public void setHtml(final boolean isHtmlFormatted) {
    this.isHtmlFormatted = isHtmlFormatted;
    if (isHtmlFormatted) {
      eol = "<br>";
      highlightStart = "<b>";
      highlightEnd = "</b>";
      documentStart = "<html>";
      documentEnd = "</html>";
    } else {
      eol = "\n";
      highlightStart = "";
      highlightEnd = "";
      documentStart = "";
      documentEnd = "";
    }
  }

  public String writeComponentLocation(final Geometry geom,
    final Coordinates p, final double tolerance) {
    final ComponentLocater locater = new ComponentLocater(geom);
    final List locs = locater.getComponents(p, tolerance);

    final StringBuffer buf = new StringBuffer();
    int count = 0;
    for (final Iterator i = locs.iterator(); i.hasNext();) {

      final GeometryLocation loc = (GeometryLocation)i.next();
      final Geometry comp = loc.getComponent();

      String path = loc.pathString();
      path = path.length() == 0 ? "" : path;
      buf.append("[" + path + "]  ");

      buf.append(comp.getGeometryType().toUpperCase());
      if (comp instanceof GeometryCollection) {
        buf.append("[" + comp.getNumGeometries() + "]");
      } else {
        buf.append("(" + comp.getVertexCount() + ")");
      }
      if (comp.getUserData() != null) {
        buf.append("  Data: ");
        buf.append(comp.getUserData().toString());
      }
      buf.append(eol);

      if (count++ > MAX_ITEMS_TO_DISPLAY) {
        buf.append(" & more..." + eol);
        break;
      }
    }
    final String locStr = buf.toString();
    if (locStr.length() == 0) {
      return null;
    }
    return locStr;
  }

  public String writeFacetLocation(final Geometry geom, final Coordinates p,
    final double tolerance) {
    final FacetLocater locater = new FacetLocater(geom);
    final List locs = locater.getLocations(p, tolerance);
    final List vertexLocs = FacetLocater.filterVertexLocations(locs);

    // only show vertices if some are present, to avoid confusing with segments
    if (!vertexLocs.isEmpty()) {
      return writeFacetLocations(vertexLocs);
    }

    // write 'em all
    return writeFacetLocations(locs);
  }

  private String writeFacetLocations(final List locs) {
    if (locs.size() <= 0) {
      return null;
    }

    final StringBuffer buf = new StringBuffer();
    boolean isFirst = true;
    int count = 0;
    for (final Iterator i = locs.iterator(); i.hasNext();) {
      final GeometryLocation loc = (GeometryLocation)i.next();

      if (!isFirst) {
        buf.append(eol);
      }

      isFirst = false;

      String compType = "";
      if (loc.getComponent() instanceof LinearRing) {
        final boolean isCCW = CGAlgorithms.isCCW(loc.getComponent()
          .getCoordinateArray());
        compType = "Ring" + (isCCW ? "-CCW" : "-CW ") + " ";
      } else if (loc.getComponent() instanceof LineString) {
        compType = "Line  ";
      } else if (loc.getComponent() instanceof Point) {
        compType = "Point ";
      }
      buf.append(compType);
      buf.append(loc.isVertex() ? "Vert" : "Seg");
      buf.append(loc.toFacetString());
      if (count++ > MAX_ITEMS_TO_DISPLAY) {
        buf.append(eol + " & more..." + eol);
        break;
      }
    }
    return buf.toString();
  }

  public String writeLocation(final Layer lyr, final Coordinates p,
    final double tolerance) {
    final Geometry geom = lyr.getGeometry();
    if (geom == null) {
      return null;
    }

    final String locStr = writeComponentLocation(geom, p, tolerance);
    final String facetStr = writeFacetLocation(geom, p, tolerance);
    if (facetStr == null) {
      return locStr;
    }
    return locStr + facetStr;
  }

  public String writeLocationString(final LayerList layers,
    final Coordinates pt, final double tolerance) {
    final StringBuffer text = new StringBuffer();
    for (int i = 0; i < layers.size(); i++) {

      final Layer lyr = layers.getLayer(i);
      final String locStr = writeLocation(lyr, pt, tolerance);
      if (locStr == null) {
        continue;
      }

      if (i > 0 && text.length() > 0) {
        text.append(eol);
        text.append(eol);
      }

      text.append(highlightStart + lyr.getName() + highlightEnd + eol);
      text.append(locStr);
    }

    if (text.length() > 0) {
      return documentStart + text.toString() + documentEnd;
    }
    return null;
  }

  public String writeSingleLocation(final Layer lyr, final Coordinates p,
    final double tolerance) {
    final Geometry geom = lyr.getGeometry();
    if (geom == null) {
      return null;
    }

    final VertexLocater locater = new VertexLocater(geom);
    final Coordinates coord = locater.getVertex(p, tolerance);
    final int index = locater.getIndex();

    if (coord == null) {
      return null;
    }
    return "[" + index + "]: " + coord.getX() + ", " + coord.getY();
  }

}
