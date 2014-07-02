package com.revolsys.io.openstreetmap.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import com.revolsys.gis.data.model.RecordIdentifier;
import com.revolsys.io.xml.StaxUtils;
import com.revolsys.jts.geom.Geometry;
import com.revolsys.jts.geom.LineString;
import com.revolsys.jts.geom.Point;

public class OsmWay extends OsmElement {

  public OsmWay() {
  }

  public OsmWay(final OsmDocument document, final XMLStreamReader in) {
    super(in);
    final List<Point> points = new ArrayList<>();
    while (StaxUtils.skipToChildStartElements(in, WAY_XML_ELEMENTS)) {
      final QName name = in.getName();
      if (name.equals(TAG)) {
        parseTag(in);
      } else if (name.equals(ND)) {
        parseNodRef(document, points, in);
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
    Geometry geometry;
    if (points.isEmpty()) {
      geometry = OsmConstants.WGS84_2D.point();
    } else if (points.size() == 1) {
      geometry = points.get(0);
    } else {
      final LineString line = OsmConstants.WGS84_2D.lineString(points);
      if (isArea() && line.isClosed()) {
        geometry = OsmConstants.WGS84_2D.polygon(line);
      } else {
        geometry = line;
      }
    }
    setGeometryValue(geometry);
  }

  @Override
  public RecordIdentifier getIdentifier() {
    final long id = getId();
    return new OsmWayIdentifier(id);
  }

  public boolean isArea() {
    if ("yes".equals(getString("area"))) {
      return true;
    } else if (Arrays.asList("bare_rock", "fell", "glacier, landuse=grass",
      "grassland", "heath", "mud", "scree", "sand", "scrub", "tree", "wetland",
        "wood").contains(getTag("natural"))) {
      return true;
    }
    return false;
  }

  private void parseNodRef(final OsmDocument document,
    final List<Point> points, final XMLStreamReader in) {
    final long nodeId = StaxUtils.getLongAttribute(in, null, "ref");
    final Point point = document.getNodePoint(nodeId);
    if (point != null && !point.isEmpty()) {
      points.add(point);
    }
    StaxUtils.skipToEndElement(in, ND);
  }

}
