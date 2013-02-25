package com.revolsys.swing.map.layer;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;

public class Project extends LayerGroup {
  private GeometryFactory geometryFactory = GeometryFactory.getFactory(3857);

  public Project() {
    super("Project");
  }

  public Project(final String name) {
    super(name);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  public Project getProject() {
    return this;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != this.geometryFactory) {
      final GeometryFactory old = this.geometryFactory;
      this.geometryFactory = geometryFactory;
      getPropertyChangeSupport().firePropertyChange("geometryFactory", old,
        this.geometryFactory);
    }
  }
  

  private BoundingBox viewBoundingBox = new BoundingBox();

  public BoundingBox getViewBoundingBox() {
    return viewBoundingBox;
  }

  public void setViewBoundingBox(BoundingBox viewBoundingBox) {
    BoundingBox oldValue = this.viewBoundingBox;

    this.viewBoundingBox = viewBoundingBox;
    getPropertyChangeSupport().firePropertyChange("viewBoundingBox", oldValue,
      viewBoundingBox);
  }
}
