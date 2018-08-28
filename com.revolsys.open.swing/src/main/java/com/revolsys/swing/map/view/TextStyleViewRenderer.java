package com.revolsys.swing.map.view;

import com.revolsys.geometry.model.Geometry;
import com.revolsys.record.Record;
import com.revolsys.swing.map.layer.record.style.TextStyle;

public abstract class TextStyleViewRenderer {
  protected TextStyle style;

  public TextStyleViewRenderer(final TextStyle style) {
    this.style = style;
  }

  public void drawText(final Record record, final Geometry geometry) {
    if (geometry != null) {
      final String label = this.style.getLabel(record);
      for (final Geometry part : geometry.geometries()) {
        drawText(label, part);
      }
    }
  }

  public abstract void drawText(String label, Geometry geometry);
}
