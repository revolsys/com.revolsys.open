package com.revolsys.gis.parallel;

import java.util.LinkedHashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.jts.geom.Geometry;

public class OutsideBoundaryObjects {
  private static final Logger LOG = LoggerFactory.getLogger(OutsideBoundaryObjects.class);

  private Set<DataObject> objects = new LinkedHashSet<DataObject>();

  private Geometry boundary;

  private Geometry preparedBoundary;

  public boolean addObject(final DataObject object) {
    return objects.add(object);
  }

  public boolean boundaryContains(final DataObject object) {
    final Geometry geometry = object.getGeometryValue();
    return boundaryContains(geometry);
  }

  public boolean boundaryContains(final Geometry geometry) {
    return geometry == null || boundary == null
      || preparedBoundary.contains(geometry);
  }

  public void clear() {
    objects = new LinkedHashSet<DataObject>();
  }

  public void expandBoundary(final Geometry geometry) {
    if (boundary == null) {
      setBoundary(geometry);
    } else {
      setBoundary(boundary.union(geometry));
    }
  }

  public Set<DataObject> getAndClearObjects() {
    final Set<DataObject> objects = this.objects;
    LOG.info("Outside boundary objects size=" + this.objects.size());
    clear();
    return objects;
  }

  public Geometry getBoundary() {
    return boundary;
  }

  public Set<DataObject> getObjects() {
    return objects;
  }

  public boolean removeObject(final DataObject object) {
    return objects.remove(object);
  }

  public void setBoundary(final Geometry boundary) {
    this.boundary = boundary;
    this.preparedBoundary = boundary.prepare();
  }

  public void setObjects(final Set<DataObject> objects) {
    this.objects = objects;
  }
}
