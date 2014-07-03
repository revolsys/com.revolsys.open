package com.revolsys.io.openstreetmap.model;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.io.xml.StaxUtils;

public class OsmNode extends OsmElement {

  public OsmNode() {
  }

  public OsmNode(final XMLStreamReader in) {
    super(in);
    final double lon = StaxUtils.getDoubleAttribute(in, null, "lon");
    final double lat = StaxUtils.getDoubleAttribute(in, null, "lat");
    setGeometryValue(OsmConstants.WGS84_2D.point(lon, lat));
    while (StaxUtils.skipToChildStartElements(in, NODE_XML_ELEMENTS)) {
      final QName name = in.getName();
      if (name.equals(TAG)) {
        parseTag(in);
      } else {
        StaxUtils.skipSubTree(in);
      }
    }
  }

  @Override
  public Identifier getIdentifier() {
    final long id = getId();
    return new OsmNodeIdentifier(id);
  }
}
