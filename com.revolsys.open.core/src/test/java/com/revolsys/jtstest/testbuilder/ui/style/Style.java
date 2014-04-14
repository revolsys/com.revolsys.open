package com.revolsys.jtstest.testbuilder.ui.style;

import java.awt.Graphics2D;

import com.revolsys.jts.geom.Geometry;
import com.revolsys.jtstest.testbuilder.Viewport;

public interface Style {
  void paint(Geometry geom, Viewport viewport, Graphics2D g)
  throws Exception;
}
