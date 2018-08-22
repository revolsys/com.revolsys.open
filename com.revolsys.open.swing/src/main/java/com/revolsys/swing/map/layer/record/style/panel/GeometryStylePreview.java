package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.LineString;
import com.revolsys.geometry.model.LinearRing;
import com.revolsys.geometry.model.Polygon;
import com.revolsys.geometry.model.impl.LineStringDouble;
import com.revolsys.geometry.model.impl.LinearRingDoubleGf;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;

public class GeometryStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  public static LineString getLineString(final int size) {
    return new LineStringDouble(2, //
      0.19 * size, 0.19 * size, //
      0.79 * size, 0.19 * size, //
      0.19 * size, 0.79 * size, //
      0.79 * size, 0.79 * size //
    );
  }

  public static Polygon getPolygon(final int size) {
    final LinearRing ring = new LinearRingDoubleGf(2, //
      0.19 * size, 0.19 * size, //
      0.79 * size, 0.19 * size, //
      0.35 * size, 0.39 * size, //
      0.79 * size, 0.59 * size, //
      0.59 * size, 0.79 * size, //
      0.39 * size, 0.79 * size, //
      0.19 * size, 0.59 * size, //
      0.19 * size, 0.39 * size, //
      0.19 * size, 0.19 * size //
    );
    return ring.newPolygon();
  }

  private final DataType geometryDataType;

  private final GeometryStyle geometryStyle;

  private final Polygon polygon;

  private LineString line = getLineString(100);

  public GeometryStylePreview(final GeometryStyle geometryStyle, final DataType geometryDataType) {
    this(geometryStyle, geometryDataType, getPolygon(100));
  }

  public GeometryStylePreview(final GeometryStyle geometryStyle, final DataType geometryDataType,
    final Polygon polygon) {
    final Dimension size = new Dimension(100, 100);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.geometryStyle = geometryStyle;
    this.geometryDataType = geometryDataType;
    this.polygon = polygon;
    if (DataTypes.LINE_STRING.equals(geometryDataType)) {

    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      this.line = this.polygon.getShell();
    }

  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;
    final Paint paint = graphics.getPaint();
    final Stroke stroke = graphics.getStroke();
    try {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      if (DataTypes.POLYGON.equals(this.geometryDataType)) {
        this.geometryStyle.fillPolygon(null, graphics, this.polygon);
      }
      this.geometryStyle.drawLineString(null, graphics, this.line);
    } finally {
      graphics.setPaint(paint);
      graphics.setStroke(stroke);
    }
  }
}
