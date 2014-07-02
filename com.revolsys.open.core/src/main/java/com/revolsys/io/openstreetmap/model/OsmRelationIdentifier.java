package com.revolsys.io.openstreetmap.model;

import com.revolsys.gis.data.model.SingleRecordIdentifier;

public class OsmRelationIdentifier extends SingleRecordIdentifier {

  public OsmRelationIdentifier(final long id) {
    super(id);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof OsmRelationIdentifier) {
      return super.equals(other);
    } else {
      return false;
    }
  }
}
