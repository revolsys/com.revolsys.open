package com.revolsys.swing.map;

import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.swing.JComponent;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.layer.LayerGroup;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;
import com.revolsys.swing.parallel.Invoke;
import com.revolsys.util.Property;
import com.revolsys.value.GlobalBooleanValue;

public class ComponentViewport2D extends Viewport2D implements PropertyChangeListener {

  private final JComponent component;

  private final GlobalBooleanValue componentResizing = new GlobalBooleanValue(false);

  public ComponentViewport2D(final Project project, final JComponent component) {
    super(project);
    this.component = component;
    Property.addListener(project, "geometryFactory", this);
    Property.addListener(project, "viewBoundingBox", this);
    component.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentResized(final ComponentEvent e) {
        if (isInitialized()) {
          try (
            BaseCloseable componentResizing = ComponentViewport2D.this.componentResizing
              .closeable(true)) {
            updateCachedFields();
          }
        }
      }
    });
  }

  public Unit<Length> getScaleUnit(final double scale) {
    final Unit<Length> lengthUnit = getGeometryFactory().getHorizontalLengthUnit();
    final Unit<Length> scaleUnit = lengthUnit.divide(scale);
    return scaleUnit;
  }

  public boolean isComponentResizing() {
    return this.componentResizing.isTrue();
  }

  @Override
  public Graphics2DViewRenderer newViewRenderer() {
    return new Graphics2DViewRenderer(this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (isInitialized() && event.getSource() == getProject()) {
      if (event.getPropertyName().equals("viewBoundingBox")) {
        // final BoundingBox boundingBox =
        // (BoundingBox)event.getNewValue();
        // if (isInitialized()) {
        // updateCachedFields();
        // } else {
        // setBoundingBoxInternal(boundingBox);
        // }
      } else {
        Invoke.later(this::updateCachedFields);
      }
    }
  }

  @Override
  protected void setGeometryFactoryPreEvent(final GeometryFactory geometryFactory) {
    if (geometryFactory.isHasHorizontalCoordinateSystem()) {
      final BoundingBox areaBoundingBox = geometryFactory.getAreaBoundingBox();
      final BoundingBox boundingBox = getBoundingBox();
      if (Property.hasValue(boundingBox)) {
        final BoundingBox newBoundingBox = boundingBox.bboxToCs(geometryFactory);
        BoundingBox intersection = newBoundingBox.bboxIntersection(areaBoundingBox);
        if (intersection.isEmpty()) {
          intersection = areaBoundingBox;
        }

        setBoundingBox(intersection);
      } else {
        setBoundingBox(areaBoundingBox);
      }
    }
  }

  @Override
  public void setInitialized(final boolean initialized) {
    if (initialized && !isInitialized()) {
      updateCachedFields();
    }
    super.setInitialized(initialized);
  }

  public void translate(final double dx, final double dy) {
    final BoundingBox boundingBox = getBoundingBox();
    final BoundingBox newBoundingBox = boundingBox.getGeometryFactory()
      .newBoundingBox(boundingBox.getMinX() + dx, boundingBox.getMinY() + dy,
        boundingBox.getMaxX() + dx, boundingBox.getMaxY() + dy);
    setBoundingBox(newBoundingBox);

  }

  @Override
  public void update() {
    this.component.repaint();
  }

  private void updateCachedFields() {
    final LayerGroup project = getProject();
    final GeometryFactory geometryFactory = project.getGeometryFactory();
    if (geometryFactory != null) {
      if (geometryFactory != getGeometryFactory()) {
        setGeometryFactory(geometryFactory);
      }
      final Insets insets = this.component.getInsets();

      final int viewWidth = this.component.getWidth() - insets.left - insets.right;
      final int viewHeight = this.component.getHeight() - insets.top - insets.bottom;

      resetView(viewWidth, viewHeight);

      update();
    }
  }

}
