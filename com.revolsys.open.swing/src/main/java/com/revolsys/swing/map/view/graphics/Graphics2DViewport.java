package com.revolsys.swing.map.view.graphics;

import java.awt.Graphics2D;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleXY;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.layer.Project;

public class Graphics2DViewport extends Viewport2D implements BaseCloseable {
  private Graphics2D graphics;

  public Graphics2DViewport(final Graphics2D graphics, final double width, final double height) {
    super(null, width, height, new BoundingBoxDoubleXY(0, 0, width, height));
    this.graphics = graphics;
  }

  public Graphics2DViewport(final Project project) {
    super(project);
  }

  public Graphics2DViewport(final Project project, final double width, final double height,
    final BoundingBox boundingBox) {
    super(project, width, height, boundingBox);
  }

  public Graphics2DViewport(final Viewport2D parentViewport) {
    super(parentViewport);
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
  public Graphics2DViewRenderer newViewRenderer() {
    return new Graphics2DViewRenderer(this, this.graphics);
  }

  protected void setGraphics(final Graphics2D graphics) {
    this.graphics = graphics;
  }

}
