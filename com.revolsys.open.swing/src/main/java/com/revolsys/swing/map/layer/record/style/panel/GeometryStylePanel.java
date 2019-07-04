package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.common.data.type.DataType;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.swing.Panels;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.renderer.GeometryStyleLayerRenderer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.util.Property;

public class GeometryStylePanel extends BaseStylePanel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final DataType geometryDataType;

  private final GeometryStyle geometryStyle;

  private final GeometryStyleLayerRenderer<?> renderer;

  private JPanel previews;

  public GeometryStylePanel(final GeometryStyleLayerRenderer<?> renderer) {
    super(renderer, true);

    this.renderer = renderer;
    this.geometryStyle = renderer.getStyle();
    this.geometryDataType = renderer.getGeometryType();
    if (this.geometryDataType != null) {
      final JPanel panel = new JPanel(new BorderLayout());
      add(panel, 1);
      final JPanel stylePanels = new JPanel(new VerticalLayout(5));
      panel.add(stylePanels, BorderLayout.CENTER);

      this.previews = Panels.titledTransparentVerticalLayout("Preview", 5);

      final JPanel previewContainer = new JPanel(new VerticalLayout());
      previewContainer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      previewContainer.add(this.previews);
      panel.add(previewContainer, BorderLayout.EAST);

      if (GeometryDataTypes.GEOMETRY.equals(this.geometryDataType)) {
        addMarkerStylePanel(stylePanels, this.geometryStyle);
        addLineStylePanel(stylePanels, this.geometryStyle);
        addPolygonStylePanel(stylePanels, this.geometryStyle);
        this.previews.add(new MarkerStylePreview(this.geometryStyle));
        addGeometryPreview(GeometryDataTypes.LINE_STRING);
        addGeometryPreview(GeometryDataTypes.POLYGON);
      } else if (GeometryDataTypes.POINT.equals(this.geometryDataType)) {
        addMarkerStylePanel(stylePanels, this.geometryStyle);
        this.previews.add(new MarkerStylePreview(this.geometryStyle));
      } else if (GeometryDataTypes.LINE_STRING.equals(this.geometryDataType)) {
        addLineStylePanel(stylePanels, this.geometryStyle);
        addGeometryPreview(GeometryDataTypes.LINE_STRING);
      } else if (GeometryDataTypes.POLYGON.equals(this.geometryDataType)) {
        addLineStylePanel(stylePanels, this.geometryStyle);
        addPolygonStylePanel(stylePanels, this.geometryStyle);
        addGeometryPreview(GeometryDataTypes.POLYGON);
      }
    }
  }

  protected void addGeometryPreview(final DataType geometryDataType) {
    final GeometryStylePreview linePreview = new GeometryStylePreview(this.geometryStyle,
      geometryDataType);
    this.previews.add(linePreview);
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      Object fieldValue = field.getFieldValue();
      if ("lineDashOffset".equals(fieldName) && !Property.hasValue(fieldValue)) {
        fieldValue = 0;
      }
      this.geometryStyle.setProperty(fieldName, fieldValue);
    } else if (source == this.geometryStyle) {
      final String name = event.getPropertyName();
      final Object value = event.getNewValue();
      setFieldValue(name, value);
    }
    if (this.previews != null) {
      for (final Component preview : this.previews.getComponents()) {
        preview.repaint();
      }
    }
  }

  @Override
  public void save() {
    super.save();
    this.renderer.setStyle(this.geometryStyle);
  }
}
