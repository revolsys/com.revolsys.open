package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.util.JavaBeanUtil;

public class GeometryStylePanel extends BaseStylePanel implements
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final GeometryStyleRenderer geometryStyleRenderer;

  private final GeometryStyle geometryStyle;

  private DataType geometryDataType;

  private JPanel previews;

  public GeometryStylePanel(final GeometryStyleRenderer geometryStyleRenderer) {
    super(geometryStyleRenderer);

    this.geometryStyleRenderer = geometryStyleRenderer;
    this.geometryStyle = geometryStyleRenderer.getStyle().clone();
    final DataObjectLayer layer = geometryStyleRenderer.getLayer();
    final DataObjectMetaData metaData = layer.getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();

    if (geometryAttribute != null) {
      this.geometryDataType = geometryAttribute.getType();
      if (DataTypes.GEOMETRY_COLLECTION.equals(this.geometryDataType)) {
        this.geometryDataType = DataTypes.GEOMETRY;
      } else if (DataTypes.MULTI_POINT.equals(this.geometryDataType)) {
        this.geometryDataType = DataTypes.POINT;
      } else if (DataTypes.MULTI_LINE_STRING.equals(this.geometryDataType)) {
        this.geometryDataType = DataTypes.LINE_STRING;
      } else if (DataTypes.MULTI_POLYGON.equals(this.geometryDataType)) {
        this.geometryDataType = DataTypes.POLYGON;
      }

      final boolean hasLineStyle = false;
      final boolean hasPolygonStyle = false;

      final JPanel panel = new JPanel(new BorderLayout());
      add(panel);
      final JPanel stylePanels = new JPanel(new VerticalLayout());
      panel.add(stylePanels, BorderLayout.CENTER);

      this.previews = new JPanel(new VerticalLayout());
      this.previews.setBorder(BorderFactory.createTitledBorder("Preview"));
      panel.add(this.previews, BorderLayout.EAST);

      if (DataTypes.GEOMETRY.equals(this.geometryDataType)) {
        addMarkerStylePanel(stylePanels, geometryStyle);
        addLineStylePanel(stylePanels, geometryStyle);
        addPolygonStylePanel(stylePanels, geometryStyle);
        this.previews.add(new MarkerStylePreview(this.geometryStyle));
        addGeometryPreview(DataTypes.LINE_STRING);
        addGeometryPreview(DataTypes.POLYGON);
      } else if (DataTypes.POINT.equals(this.geometryDataType)) {
        addMarkerStylePanel(stylePanels, geometryStyle);
        this.previews.add(new MarkerStylePreview(this.geometryStyle));
      } else if (DataTypes.LINE_STRING.equals(this.geometryDataType)) {
        addLineStylePanel(stylePanels, geometryStyle);
        addGeometryPreview(DataTypes.LINE_STRING);
      } else if (DataTypes.POLYGON.equals(this.geometryDataType)) {
        addLineStylePanel(stylePanels, geometryStyle);
        addPolygonStylePanel(stylePanels, geometryStyle);
        addGeometryPreview(DataTypes.POLYGON);
      }

      if (hasLineStyle) {
      }
      if (hasPolygonStyle) {
      }
    }
  }

  protected void addGeometryPreview(final DataType geometryDataType) {
    final GeometryStylePreview linePreview = new GeometryStylePreview(
      this.geometryStyle, geometryDataType);
    this.previews.add(linePreview);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      JavaBeanUtil.setProperty(this.geometryStyle, fieldName, fieldValue);
    }
    for (final Component preview : this.previews.getComponents()) {
      preview.repaint();
    }
  }

  @Override
  public void save() {
    super.save();
    this.geometryStyleRenderer.setStyle(this.geometryStyle);
  }
}
