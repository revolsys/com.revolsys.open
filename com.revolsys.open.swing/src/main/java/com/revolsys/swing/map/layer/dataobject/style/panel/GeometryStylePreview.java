package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;

public class GeometryStylePreview extends JPanel {
 private static final long serialVersionUID = 1L;

  public static GeneralPath getLineShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(19, 19);
    path.lineTo(79, 19);
    path.lineTo(19, 79);
    path.lineTo(79, 79);
    return path;
  }

  public static GeneralPath getPolygonShape() {
    final GeneralPath path = new GeneralPath();
    path.moveTo(19, 19);
    path.lineTo(79, 19);
    path.lineTo(35, 39);
    path.lineTo(79, 59);
    path.lineTo(59, 79);
    path.lineTo(39, 79);
    path.lineTo(19, 59);
    path.lineTo(19, 39);
    path.closePath();
    return path;
  }

  private Shape shape = getLineShape();

  private GeometryStyle geometryStyle;

  private DataType geometryDataType;

  public GeometryStylePreview(GeometryStyle geometryStyle,
    DataType geometryDataType) {
    Dimension size = new Dimension(100, 100);
    setPreferredSize(size);
    setMinimumSize(size);
    setMaximumSize(size);
    setBackground(Color.WHITE);
    setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY));
    this.geometryStyle = geometryStyle;
    this.geometryDataType = geometryDataType;
    if (DataTypes.LINE_STRING.equals(geometryDataType)) {
      shape = getLineShape();
    } else if (DataTypes.POLYGON.equals(geometryDataType)) {
      shape = getPolygonShape();
    }
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    final Graphics2D graphics = (Graphics2D)g;
    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
      RenderingHints.VALUE_ANTIALIAS_ON);

    if (DataTypes.POLYGON.equals(geometryDataType)) {
      geometryStyle.setFillStyle(null, graphics);
      graphics.fill(shape);
    }
    geometryStyle.setLineStyle(null, graphics);
    graphics.draw(shape);
  }
}
