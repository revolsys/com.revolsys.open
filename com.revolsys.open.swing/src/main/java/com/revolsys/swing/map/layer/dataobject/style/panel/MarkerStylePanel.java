package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.awt.WebColors;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.map.layer.dataobject.AbstractDataObjectLayer;
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
    final AbstractDataObjectLayer layer = markerStyleRenderer.getLayer();
    final DataObjectMetaData metaData = layer.getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();

    if (geometryAttribute != null) {

      final JPanel panel = new JPanel(new BorderLayout());
      panel.setBackground(WebColors.White);
      add(panel, 1);
      final JPanel stylePanels = new JPanel(new VerticalLayout(5));
      stylePanels.setBackground(WebColors.White);
      panel.add(stylePanels, BorderLayout.CENTER);

      this.previews = new JPanel(new VerticalLayout(5));
      SwingUtil.setTitledBorder(previews, "Preview");

      final JPanel previewContainer = new JPanel(new VerticalLayout());
      previewContainer.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
      previewContainer.setBackground(WebColors.White);
      previewContainer.add(previews);
      panel.add(previewContainer, BorderLayout.EAST);

      addMarkerStylePanel(stylePanels, markerStyle);
      this.previews.add(new MarkerStylePreview(this.markerStyle));
    }
  }

  @Override
  public void doPropertyChange(final PropertyChangeEvent event) {
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
