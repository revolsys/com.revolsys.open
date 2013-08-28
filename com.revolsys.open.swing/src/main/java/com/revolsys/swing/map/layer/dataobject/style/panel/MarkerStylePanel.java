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
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.MarkerStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.util.JavaBeanUtil;

public class MarkerStylePanel extends BaseStylePanel implements
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final MarkerStyleRenderer geometryStyleRenderer;

  private final MarkerStyle markerStyle;

  private JPanel previews;

  public MarkerStylePanel(final MarkerStyleRenderer markerStyleRenderer) {
    super(markerStyleRenderer);

    this.geometryStyleRenderer = markerStyleRenderer;
    this.markerStyle = markerStyleRenderer.getStyle().clone();
    final DataObjectLayer layer = markerStyleRenderer.getLayer();
    final DataObjectMetaData metaData = layer.getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();

    if (geometryAttribute != null) {

      final JPanel panel = new JPanel(new BorderLayout());
      add(panel);
      final JPanel stylePanels = new JPanel(new VerticalLayout());
      panel.add(stylePanels, BorderLayout.CENTER);

      this.previews = new JPanel(new VerticalLayout());
      this.previews.setBorder(BorderFactory.createTitledBorder("Preview"));
      panel.add(this.previews, BorderLayout.EAST);

      addMarkerStylePanel(stylePanels, markerStyle);
      this.previews.add(new MarkerStylePreview(this.markerStyle));
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      JavaBeanUtil.setProperty(this.markerStyle, fieldName, fieldValue);
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
