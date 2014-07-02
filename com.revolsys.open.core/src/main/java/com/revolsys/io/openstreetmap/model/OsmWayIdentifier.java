package com.revolsys.io.openstreetmap.model;

import com.revolsys.gis.data.model.SingleRecordIdentifier;

public class OsmWayIdentifier extends SingleRecordIdentifier {

  public OsmWayIdentifier(final long id) {
    super(id);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof OsmWayIdentifier) {
      return super.equals(other);
    } else {
      return false;
    }
  }
}
