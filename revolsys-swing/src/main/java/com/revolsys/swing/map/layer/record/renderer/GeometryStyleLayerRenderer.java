package com.revolsys.swing.map.layer.record.renderer;

import org.jeometry.common.data.type.DataType;

import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;

public interface GeometryStyleLayerRenderer<L extends Layer> extends LayerRenderer<L> {

  DataType getGeometryType();

  GeometryStyle getStyle();

  void setStyle(GeometryStyle style);

}
