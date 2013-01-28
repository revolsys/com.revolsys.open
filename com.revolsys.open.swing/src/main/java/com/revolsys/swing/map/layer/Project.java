package com.revolsys.swing.map.layer;

import com.revolsys.gis.cs.GeometryFactory;

public class Project extends LayerGroup {
  private GeometryFactory geometryFactory;

  public Project() {
    super("Project");
  }

  public Project(final String name) {
    super(name);
  }

  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public Project getProject() {
    return this;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != this.geometryFactory) {
      final GeometryFactory old = this.geometryFactory;
      this.geometryFactory = geometryFactory;
      getPropertyChangeSupport().firePropertyChange("geometryFactory", old,
        this.geometryFactory);
    }
  }
}
