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
import com.revolsys.swing.map.layer.dataobject.renderer.TextStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.TextStyle;
import com.revolsys.util.JavaBeanUtil;

public class TextStylePanel extends BaseStylePanel implements
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final TextStyleRenderer textStyleRenderer;

  private final TextStyle textStyle;

  private JPanel previews;

  public TextStylePanel(final TextStyleRenderer textStyleRenderer) {
    super(textStyleRenderer);

    this.textStyleRenderer = textStyleRenderer;
    this.textStyle = textStyleRenderer.getStyle().clone();
    final DataObjectLayer layer = textStyleRenderer.getLayer();
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

      addPanel(stylePanels, "Text Label", textStyle, "textName", "textSize",
        "textFaceName");
      addPanel(stylePanels, "Text Color", textStyle, "textFill",
        "textBoxColor", "textHaloFill", "textHaloRadius");
      addPanel(stylePanels, "Text Position", textStyle,
        "textHorizontalAlignment", "textVerticalAlignment", "textDx", "textDy",
        "textOrientationType", "textOrientation", "textPlacementType");

      // this.previews.add(new TextStylePreview(this.textStyle));
    }
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      JavaBeanUtil.setProperty(this.textStyle, fieldName, fieldValue);
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
