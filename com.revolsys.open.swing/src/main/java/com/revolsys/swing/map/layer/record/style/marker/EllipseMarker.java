package com.revolsys.swing.map.layer.record.style.marker;

import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.view.ViewRenderer;

public class EllipseMarker extends AbstractMarker {

  public EllipseMarker() {
    super("ellipse");
  }

  public EllipseMarker(final String name) {
    super(name);
  }

  @Override
  public boolean equals(final Object object) {
    if (object instanceof EllipseMarker) {
      final EllipseMarker marker = (EllipseMarker)object;
      return getName().equals(marker.getName());
    } else {
      return false;
    }
  }

  @Override
  public String getTypeName() {
    return "markerEllipse";
  }

  @Override
  public boolean isUseMarkerName() {
    return true;
  }

  @Override
  public MarkerRenderer newMarkerRenderer(final ViewRenderer view, final MarkerStyle style) {
    return view.newMarkerRendererEllipse(style);
  }
}
