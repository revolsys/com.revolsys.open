package com.revolsys.swing.map;

import java.awt.Graphics2D;

import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.io.BaseCloseable;
import com.revolsys.swing.map.layer.Project;
import com.revolsys.swing.map.view.graphics.Graphics2DViewRenderer;

public class Graphics2DViewport extends Viewport2D implements BaseCloseable {
  private Graphics2D graphics;

  public Graphics2DViewport() {
  }

  public Graphics2DViewport(final Project project) {
    super(project);
  }

  public Graphics2DViewport(final Project project, final int width, final int height,
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
  public Graphics2DViewRenderer newViewRenderer() {
    return new Graphics2DViewRenderer(this, this.graphics);
  }

  protected void setGraphics(final Graphics2D graphics) {
    this.graphics = graphics;
  }

}
