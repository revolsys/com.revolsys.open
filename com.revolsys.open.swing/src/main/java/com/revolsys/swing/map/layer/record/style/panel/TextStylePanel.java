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
import com.revolsys.swing.map.layer.record.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.util.Property;

public class TextStylePanel extends BaseStylePanel implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private JPanel previews;

  final TextStyle textStyle;

  private final TextStyleRenderer textStyleRenderer;

  public TextStylePanel(final TextStyleRenderer textStyleRenderer) {
    super(textStyleRenderer, true);

    this.textStyleRenderer = textStyleRenderer;
    this.textStyle = textStyleRenderer.getStyle();
    final AbstractRecordLayer layer = textStyleRenderer.getLayer();
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

      addTextStylePanels(stylePanels, this.textStyle);

      this.previews.add(new TextStylePreview(this.textStyle));
    }
  }

  @Override
  protected Field newField(final String fieldName, final Class<?> fieldClass, final Object value) {
    if (fieldName.equals("textName")) {
      final AbstractRecordLayer layer = getLayer();
      final TextNameField textNameField = new TextNameField(layer, fieldName, value);
      Property.addListener(textNameField, fieldName, this);
      return textNameField;
    } else {
      return super.newField(fieldName, fieldClass, value);
    }
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      this.textStyle.setProperty(fieldName, fieldValue);
    } else if (source == this.textStyle) {
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
    this.textStyleRenderer.setStyle(this.textStyle);
  }
}
