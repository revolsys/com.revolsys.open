package com.revolsys.swing.map.layer;

import java.lang.ref.WeakReference;

import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;

public class Project extends LayerGroup {

  private static WeakReference<Project> project = new WeakReference<Project>(
    null);

  public static Project get() {
    return Project.project.get();
  }

  public static void set(final Project project) {
    Project.project = new WeakReference<Project>(project);
  }

  private GeometryFactory geometryFactory = GeometryFactory.getFactory(3005); //  3857

  private final LayerGroup baseMapLayers = new LayerGroup("Base Maps");

  private BoundingBox viewBoundingBox = new BoundingBox();

  public Project() {
    this("Project");
  }

  public Project(final String name) {
    super(name);
    baseMapLayers.setLayerGroup(this);
    set(this);
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return geometryFactory;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <V extends Layer> V getLayer(final String name) {
    if (name.equals("Base Maps")) {
      return (V)baseMapLayers;
    } else {
      return (V)super.getLayer(name);
    }
  }

  @Override
  public Project getProject() {
    return this;
  }

  public BoundingBox getViewBoundingBox() {
    return viewBoundingBox;
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

  public void setViewBoundingBox(final BoundingBox viewBoundingBox) {
    if (!viewBoundingBox.isNull()) {
      final BoundingBox oldValue = this.viewBoundingBox;

      this.viewBoundingBox = viewBoundingBox;
      getPropertyChangeSupport().firePropertyChange("viewBoundingBox",
        oldValue, viewBoundingBox);
    }
  }
}
