package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.record.schema.FieldDefinition;
import com.revolsys.record.schema.RecordDefinition;
import com.revolsys.swing.Panels;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;

public class MarkerStylePanel extends BaseStylePanel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final MarkerStyleRenderer geometryStyleRenderer;

  private final MarkerStyle markerStyle;

  private JPanel previews;

  public MarkerStylePanel(final MarkerStyleRenderer markerStyleRenderer) {
    super(markerStyleRenderer, true);

    this.geometryStyleRenderer = markerStyleRenderer;
    this.markerStyle = markerStyleRenderer.getStyle();
    final AbstractRecordLayer layer = markerStyleRenderer.getLayer();
    final RecordDefinition recordDefinition = layer.getRecordDefinition();
    final FieldDefinition geometryField = recordDefinition.getGeometryField();

    if (geometryField != null) {

      final JPanel panel = new JPanel(new BorderLayout());
      add(panel, 1);
      final JPanel stylePanels = new JPanel(new VerticalLayout(5));
      panel.add(stylePanels, BorderLayout.CENTER);

      this.previews = Panels.titledTransparentVerticalLayout("Preview", 5);

      final JPanel previewContainer = new JPanel(new VerticalLayout());
      previewContainer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      previewContainer.add(this.previews);
      panel.add(previewContainer, BorderLayout.EAST);

      addMarkerStylePanel(stylePanels, this.markerStyle);
      this.previews.add(new MarkerStylePreview(this.markerStyle));
    }
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      this.markerStyle.setProperty(fieldName, fieldValue);
    } else if (source == this.markerStyle) {
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
    this.geometryStyleRenderer.setStyle(this.markerStyle);
  }
}
