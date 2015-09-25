package com.revolsys.record.io.format.openstreetmap.model;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;

import com.revolsys.identifier.Identifier;
import com.revolsys.record.io.format.xml.StaxUtils;

public class OsmRelation extends OsmElement {

  public OsmRelation() {
  }

  public OsmRelation(final OsmElement element) {
    super(element);
  }

  public OsmRelation(final XMLStreamReader in) {
    super(in);
    while (StaxUtils.skipToChildStartElements(in, RELATION_XML_ELEMENTS)) {
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
    return new OsmRelationIdentifier(id);
  }

}
