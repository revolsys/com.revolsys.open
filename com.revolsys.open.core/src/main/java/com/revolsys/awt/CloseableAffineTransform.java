package com.revolsys.awt;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import com.revolsys.io.BaseCloseable;

public class CloseableAffineTransform extends AffineTransform implements BaseCloseable {
  private static final long serialVersionUID = 1L;

  private final AffineTransform originalTransform;

  private final Graphics2D graphics;

  public CloseableAffineTransform(final Graphics2D graphics) {
    super(graphics.getTransform());
    this.graphics = graphics;
    this.originalTransform = graphics.getTransform();
    graphics.setTransform(this);
  }

  public CloseableAffineTransform(final Graphics2D graphics, final AffineTransform newTransform) {
    super(newTransform);
    this.graphics = graphics;
    this.originalTransform = graphics.getTransform();
    graphics.setTransform(this);
  }

  @Override
  public void close() {
    this.graphics.setTransform(this.originalTransform);
  }

  public AffineTransform getOriginalTransform() {
    return this.originalTransform;
  }
}
