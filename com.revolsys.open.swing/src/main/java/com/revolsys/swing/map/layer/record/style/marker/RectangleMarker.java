package com.revolsys.swing.map.layer.record.style.marker;

import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class RectangleMarker extends AbstractMarker {

  public RectangleMarker() {
    super("rectangle");
  }

  public RectangleMarker(final String name) {
    super(name);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof RectangleMarker) {
      final RectangleMarker marker = (RectangleMarker)object;
      return getName().equals(marker.getName());
    } else {
      return false;
    }
  }

  @Override
  public String getTypeName() {
    return "markerRectangle";
  }

  @Override
  public boolean isUseMarkerName() {
    return true;
  }

  @Override
  public MarkerRenderer newMarkerRenderer(final ViewRenderer view, final MarkerStyle style) {
    return view.newMarkerRendererRectangle(style);
  }
}
