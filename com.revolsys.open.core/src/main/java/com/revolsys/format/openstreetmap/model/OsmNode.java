package com.revolsys.format.openstreetmap.model;

import java.util.Date;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import com.revolsys.data.identifier.Identifier;
import com.revolsys.format.xml.StaxUtils;

public class OsmNode extends OsmElement {

  public OsmNode() {
  }

  public OsmNode(final long id, final boolean visible, final int version,
    final long changeset, final Date timestamp, final String user,
    final int uid, final Map<String, String> tags, final double x,
    final double y) {
    super(id, visible, version, changeset, timestamp, user, uid, tags);
    setGeometryValue(OsmConstants.WGS84_2D.point(x, y));
  }

  public OsmNode(final OsmElement element) {
    super(element);
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
