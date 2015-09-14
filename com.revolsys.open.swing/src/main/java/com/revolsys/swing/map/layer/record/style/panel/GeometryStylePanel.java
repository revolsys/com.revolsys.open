package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.awt.WebColors;
import com.revolsys.datatype.DataType;
import com.revolsys.datatype.DataTypes;
import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.JavaBeanUtil;

public class GeometryStylePanel extends BaseStylePanel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private DataType geometryDataType;

  private final GeometryStyle geometryStyle;

  private final GeometryStyleRenderer geometryStyleRenderer;

  private JPanel previews;

  public GeometryStylePanel(final GeometryStyleRenderer geometryStyleRenderer) {
    super(geometryStyleRenderer);

    this.geometryStyleRenderer = geometryStyleRenderer;
    this.geometryStyle = geometryStyleRenderer.getStyle().clone();
    final AbstractRecordLayer layer = geometryStyleRenderer.getLayer();
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    final FieldDefinition geometryField = recordDefinition.getGeometryField();

    if (geometryField != null) {
      this.geometryDataType = geometryField.getType();
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
      panel.setBackground(WebColors.White);
      add(panel, 1);
      final JPanel stylePanels = new JPanel(new VerticalLayout(5));
      stylePanels.setBackground(WebColors.White);
      panel.add(stylePanels, BorderLayout.CENTER);

      this.previews = new JPanel(new VerticalLayout(5));
      SwingUtil.setTitledBorder(this.previews, "Preview");

      final JPanel previewContainer = new JPanel(new VerticalLayout());
      previewContainer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      previewContainer.setBackground(WebColors.White);
      previewContainer.add(this.previews);
      panel.add(previewContainer, BorderLayout.EAST);

      if (DataTypes.GEOMETRY.equals(this.geometryDataType)) {
        addMarkerStylePanel(stylePanels, this.geometryStyle);
        addLineStylePanel(stylePanels, this.geometryStyle);
        addPolygonStylePanel(stylePanels, this.geometryStyle);
        this.previews.add(new MarkerStylePreview(this.geometryStyle));
        addGeometryPreview(DataTypes.LINE_STRING);
        addGeometryPreview(DataTypes.POLYGON);
      } else if (DataTypes.POINT.equals(this.geometryDataType)) {
        addMarkerStylePanel(stylePanels, this.geometryStyle);
        this.previews.add(new MarkerStylePreview(this.geometryStyle));
      } else if (DataTypes.LINE_STRING.equals(this.geometryDataType)) {
        addLineStylePanel(stylePanels, this.geometryStyle);
        addGeometryPreview(DataTypes.LINE_STRING);
      } else if (DataTypes.POLYGON.equals(this.geometryDataType)) {
        addLineStylePanel(stylePanels, this.geometryStyle);
        addPolygonStylePanel(stylePanels, this.geometryStyle);
        addGeometryPreview(DataTypes.POLYGON);
      }

      if (hasLineStyle) {
      }
      if (hasPolygonStyle) {
      }
    }
  }

  protected void addGeometryPreview(final DataType geometryDataType) {
    final GeometryStylePreview linePreview = new GeometryStylePreview(this.geometryStyle,
      geometryDataType);
    this.previews.add(linePreview);
  }

  @Override
  public void doPropertyChange(final PropertyChangeEvent event) {
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
