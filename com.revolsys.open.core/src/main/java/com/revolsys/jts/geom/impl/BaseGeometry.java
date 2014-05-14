package com.revolsys.jts.geom.impl;

import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Envelope;

public abstract class BaseGeometry extends AbstractGeometry {

  /**
   *  The bounding box of this <code>Geometry</code>.
   */
  private BoundingBox boundingBox;

  /**
   * An object reference which can be used to carry ancillary data defined
   * by the client.
   */
  private Object userData;

  @Override
  public BoundingBox getBoundingBox() {
    if (boundingBox == null) {
      if (isEmpty()) {
        boundingBox = new Envelope(getGeometryFactory());
      } else {
        boundingBox = computeBoundingBox();
      }
    }
    return boundingBox;
  }

  /**
   * Gets the user data object for this geometry, if any.
   *
   * @return the user data object, or <code>null</code> if none set
   */
  @Override
  public Object getUserData() {
    return userData;
  }

  /**
   * A simple scheme for applications to add their own custom data to a Geometry.
   * An example use might be to add an object representing a Point Reference System.
   * <p>
   * Note that user data objects are not present in geometries created by
   * construction methods.
   *
   * @param userData an object, the semantics for which are defined by the
   * application using this Geometry
   */
  @Override
  public void setUserData(final Object userData) {
    this.userData = userData;
  }

}
