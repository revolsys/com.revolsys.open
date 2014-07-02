package com.revolsys.io.openstreetmap.model;

import com.revolsys.gis.data.model.SingleRecordIdentifier;

public class OsmNodeIdentifier extends SingleRecordIdentifier {

  public OsmNodeIdentifier(final long id) {
    super(id);
  }

  @Override
  public boolean equals(final Object other) {
    if (other instanceof OsmNodeIdentifier) {
      return super.equals(other);
    } else {
      return false;
    }
  }
}
