package com.revolsys.swing.map.layer;

public class NullLayer extends AbstractLayer {
  public static final NullLayer INSTANCE = new NullLayer();

  private NullLayer() {
    super("nullLayer");
    setName("None");
  }
}
