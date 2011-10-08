package com.revolsys.gis.data.io;

import java.util.LinkedHashSet;
import java.util.Set;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.io.DelegatingWriter;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;

public class OutsideBoundaryWriter extends DelegatingWriter<DataObject> {

  private Set<DataObject> outsideBoundaryObjects = new LinkedHashSet<DataObject>();

  private Geometry boundary;

  private PreparedGeometry preparedBoundary;;

  public void expandBoundary(final Geometry geometry) {
    if (boundary == null) {
      setBoundary(geometry);
    } else {
      setBoundary(boundary.union(geometry));
    }
  }

  @Override
  public void close() {
    for (DataObject object : outsideBoundaryObjects) {
      super.write(object);
    }
    super.close();
  }

  public Geometry getBoundary() {
    return boundary;
  }

  public Set<DataObject> getOutsideBoundaryObjects() {
    return outsideBoundaryObjects;
  }

  public void setBoundary(final Geometry boundary) {
    this.boundary = boundary;
    this.preparedBoundary = PreparedGeometryFactory.prepare(boundary);
  }

  public void setOutsideBoundaryObjects(
    final Set<DataObject> outsideBoundaryObjects) {
    this.outsideBoundaryObjects = outsideBoundaryObjects;
  }

  public void clearOutsideBoundaryObjects() {
    outsideBoundaryObjects = new LinkedHashSet<DataObject>();
  }

  @Override
  public void write(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    if (geometry == null || boundary == null
      || preparedBoundary.contains(geometry)) {
      outsideBoundaryObjects.remove(object);
      super.write(object);
    } else {
      outsideBoundaryObjects.add(object);
    }
  }

  public Set<DataObject> getAndClearOutsideBoundaryObjects() {
    Set<DataObject> objects = outsideBoundaryObjects;
    clearOutsideBoundaryObjects();
    return objects;
  }
}
