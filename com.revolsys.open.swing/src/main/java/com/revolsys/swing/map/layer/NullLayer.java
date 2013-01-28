package com.revolsys.swing.map.layer;

import com.revolsys.gis.cs.BoundingBox;


public class NullLayer extends AbstractLayer {
  public static final NullLayer INSTANCE = new NullLayer();

  private NullLayer() {
    super("None");
  }
  
}
