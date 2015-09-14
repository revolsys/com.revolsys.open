package com.revolsys.format.openstreetmap.model;

import com.revolsys.identifier.SingleIdentifier;

public class OsmNodeIdentifier extends SingleIdentifier {

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
