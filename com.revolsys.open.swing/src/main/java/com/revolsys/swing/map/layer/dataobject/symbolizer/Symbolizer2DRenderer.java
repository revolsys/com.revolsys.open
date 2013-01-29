package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.awt.Graphics2D;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.symbolizer.Symbolizer;

public interface Symbolizer2DRenderer<T extends Symbolizer> {

  void render(Viewport2D viewport, Graphics2D graphics,DataObject dataObject, T style);

}
