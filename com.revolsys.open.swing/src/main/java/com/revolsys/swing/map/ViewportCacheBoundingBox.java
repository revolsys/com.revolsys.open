package com.revolsys.swing.map;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.BoundingBoxProxy;
import com.revolsys.geometry.model.GeometryFactory;

public class ViewportCacheBoundingBox implements BoundingBoxProxy {
  private BoundingBox boundingBox = BoundingBox.empty();

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_2D;

  private GeometryFactory geometryFactory2dFloating = GeometryFactory.DEFAULT_2D;

  private int viewWidthPixels;

  private int viewHeightPixels;

  private double unitsPerPixel;

  private double metresPerPixel;

  private double scale = 1;

  private double modelUnitsPerViewUnit = 1;

  public ViewportCacheBoundingBox() {
  }

  public ViewportCacheBoundingBox(final int width, final int height) {
    this.geometryFactory = GeometryFactory.DEFAULT_2D;
    this.boundingBox = this.geometryFactory.newBoundingBox(0, 0, width, height);
    this.geometryFactory2dFloating = GeometryFactory.DEFAULT_2D;
    this.viewWidthPixels = width;
    this.viewHeightPixels = height;
    this.unitsPerPixel = 1;
    this.metresPerPixel = 1;
    this.scale = 1;
    this.modelUnitsPerViewUnit = 1;
  }

  public ViewportCacheBoundingBox(final Viewport2D viewport) {
    this.boundingBox = viewport.getBoundingBox();
    this.geometryFactory = viewport.getGeometryFactory();
    this.geometryFactory2dFloating = viewport.getGeometryFactory2dFloating();
    this.viewWidthPixels = (int)Math.ceil(viewport.getViewWidthPixels());
    this.viewHeightPixels = (int)Math.ceil(viewport.getViewHeightPixels());
    this.unitsPerPixel = viewport.getUnitsPerPixel();
    this.metresPerPixel = viewport.getMetresPerPixel();
    this.scale = viewport.getScale();
    this.modelUnitsPerViewUnit = this.boundingBox.getHeight() / this.viewHeightPixels;
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public GeometryFactory getGeometryFactory2dFloating() {
    return this.geometryFactory2dFloating;
  }

  public double getMetresPerPixel() {
    return this.metresPerPixel;
  }

  public double getModelUnitsPerViewUnit() {
    return this.modelUnitsPerViewUnit;
  }

  public double getScale() {
    return this.scale;
  }

  public double getUnitsPerPixel() {
    return this.unitsPerPixel;
  }

  public int getViewHeightPixels() {
    return this.viewHeightPixels;
  }

  public int getViewWidthPixels() {
    return this.viewWidthPixels;
  }

  @Override
  public String toString() {
    return getBoundingBox() + " " + this.viewWidthPixels + "x" + this.viewHeightPixels;
  }
}
