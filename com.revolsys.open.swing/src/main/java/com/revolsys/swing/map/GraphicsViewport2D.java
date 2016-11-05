package com.revolsys.swing.map;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.revolsys.awt.CloseableAffineTransform;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.layer.Project;

public class GraphicsViewport2D extends Viewport2D implements BaseCloseable {
  private Graphics2D graphics;

  private AffineTransform graphicsTransform;

  private AffineTransform graphicsModelTransform;

  public GraphicsViewport2D() {
  }

  public GraphicsViewport2D(final Project project) {
    super(project);
  }

  public GraphicsViewport2D(final Project project, final int width, final int height,
    final BoundingBox boundingBox) {
    super(project, width, height, boundingBox);
  }

  @Override
  public void close() {
    if (this.graphics != null) {
      this.graphics.dispose();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    super.finalize();
    close();
  }

  @Override
  public Graphics2D getGraphics() {
    return this.graphics;
  }

  protected void setGraphics(final Graphics2D graphics) {
    this.graphics = graphics;
    if (graphics == null) {
      this.graphicsTransform = null;
    } else {
      this.graphicsTransform = graphics.getTransform();
    }
    updateGraphicsTransform();
  }

  @Override
  protected void setModelToScreenTransform(final AffineTransform modelToScreenTransform) {
    super.setModelToScreenTransform(modelToScreenTransform);
    updateGraphicsTransform();
  }

  @Override
  public BaseCloseable setUseModelCoordinates(final boolean useModelCoordinates) {
    final Graphics2D graphics = getGraphics();
    return setUseModelCoordinates(graphics, useModelCoordinates);
  }

  @Override
  public BaseCloseable setUseModelCoordinates(final Graphics2D graphics,
    final boolean useModelCoordinates) {
    if (graphics == null) {
      return null;
    } else {
      AffineTransform newTransform;
      if (useModelCoordinates) {
        newTransform = this.graphicsModelTransform;
      } else {
        newTransform = this.graphicsTransform;
      }
      if (newTransform == null) {
        return new CloseableAffineTransform(graphics);
      } else {
        return new CloseableAffineTransform(graphics, newTransform);
      }
    }
  }

  private void updateGraphicsTransform() {
    final AffineTransform modelToScreenTransform = getModelToScreenTransform();
    if (modelToScreenTransform == null || this.graphicsTransform == null) {
      this.graphicsModelTransform = null;
    } else {
      final AffineTransform transform = (AffineTransform)this.graphicsTransform.clone();
      transform.concatenate(modelToScreenTransform);
      this.graphicsModelTransform = transform;
    }
  }
}
