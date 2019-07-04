package com.revolsys.swing.map.layer.grid;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.geometry.model.GeometryDataTypes;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.swing.Panels;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.swing.map.layer.record.style.panel.BaseStylePanel;
import com.revolsys.swing.map.layer.record.style.panel.GeometryStylePreview;
import com.revolsys.swing.map.layer.record.style.panel.TextStylePreview;
import com.revolsys.util.Property;

public class GridLayerStylePanel extends BaseStylePanel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final GeometryStyle geometryStyle;

  private final TextStyle textStyle;

  private final JPanel previews;

  private final GridLayerRenderer gridLayerRenderer;

  public GridLayerStylePanel(final GridLayerRenderer gridLayerRenderer) {
    super(gridLayerRenderer, false);
    this.gridLayerRenderer = gridLayerRenderer;
    this.geometryStyle = gridLayerRenderer.getGeometryStyle();
    this.textStyle = gridLayerRenderer.getTextStyle();

    final JPanel panel = new JPanel(new BorderLayout());
    add(panel, 1);
    final JPanel stylePanels = new JPanel(new VerticalLayout(5));
    panel.add(stylePanels, BorderLayout.CENTER);

    this.previews = Panels.titledTransparentVerticalLayout("Preview", 5);
    final GeometryStylePreview geometryPreview = new GeometryStylePreview(this.geometryStyle,
      GeometryDataTypes.POLYGON, GeometryFactory.DEFAULT_2D.newRectangleCorners(19, 19, 60, 60));
    this.previews.add(geometryPreview);
    this.previews.add(new TextStylePreview(this.textStyle));

    final JPanel previewContainer = new JPanel(new VerticalLayout());
    previewContainer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
    previewContainer.add(this.previews);
    panel.add(previewContainer, BorderLayout.EAST);

    addLineStylePanel(stylePanels, this.geometryStyle);

    addTextStylePanels(stylePanels, this.textStyle);
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
      this.textStyle.setProperty(fieldName, fieldValue);
    } else if (source == this.geometryStyle || source == this.textStyle) {
      final String name = event.getPropertyName();
      final Object value = event.getNewValue();
      setFieldValue(name, value);
    }
    for (final Component preview : this.previews.getComponents()) {
      preview.repaint();
    }
  }

  @Override
  public void save() {
    super.save();
    this.gridLayerRenderer.setGeometryStyle(this.geometryStyle);
    this.gridLayerRenderer.setTextStyle(this.textStyle);
  }
}
