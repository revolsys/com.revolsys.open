package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;

public class GeometryStylePreview extends JPanel {
  private static final long serialVersionUID = 1L;

  public static GeneralPath getLineShape(final int size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0.19 * size, 0.19 * size);
    path.lineTo(0.79 * size, 0.19 * size);
    path.lineTo(0.19 * size, 0.79 * size);
    path.lineTo(0.79 * size, 0.79 * size);
    return path;
  }

  public static GeneralPath getPolygonShape(final int size) {
    final GeneralPath path = new GeneralPath();
    path.moveTo(0.19 * size, 0.19 * size);
    path.lineTo(0.79 * size, 0.19 * size);
    path.lineTo(0.35 * size, 0.39 * size);
    path.lineTo(0.79 * size, 0.59 * size);
    path.lineTo(0.59 * size, 0.79 * size);
    path.lineTo(0.39 * size, 0.79 * size);
    path.lineTo(0.19 * size, 0.59 * size);
    path.lineTo(0.19 * size, 0.39 * size);
    path.closePath();
    return path;
  }

  private Shape shape = getLineShape(100);

  private final GeometryStyle geometryStyle;

  private final DataType geometryDataType;

  public GeometryStylePreview(final GeometryStyle geometryStyle,
    final DataType geometryDataType) {
    final Dimension size = new Dimension(100, 100);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.geometryStyle = geometryStyle;
    this.geometryDataType = geometryDataType;
    if (DataTypes.LINE_STRING.equals(geometryDataType)) {
      this.shape = getLineShape(100);
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      this.shape = getPolygonShape(100);
    }
  }

  @Override
  protected void paintComponent(final Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;
    final Paint paint = graphics.getPaint();
    final Stroke stroke = graphics.getStroke();
    try {
      graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
        RenderingHints.VALUE_ANTIALIAS_ON);

      if (DataTypes.POLYGON.equals(this.geometryDataType)) {
        this.geometryStyle.setFillStyle(null, graphics);
        graphics.fill(this.shape);
      }
      this.geometryStyle.setLineStyle(null, graphics);
      graphics.draw(this.shape);
    } finally {
      graphics.setPaint(paint);
      graphics.setStroke(stroke);
    }
  }
}
