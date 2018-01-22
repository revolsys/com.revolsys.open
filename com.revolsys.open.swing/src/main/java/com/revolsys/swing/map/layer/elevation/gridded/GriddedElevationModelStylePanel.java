package com.revolsys.swing.map.layer.elevation.gridded;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.elevation.gridded.rasterizer.ColorGradientGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.ColorGriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.GriddedElevationModelRasterizer;
import com.revolsys.elevation.gridded.rasterizer.HillShadeGriddedElevationModelRasterizer;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.PercentSlider;
import com.revolsys.swing.map.layer.elevation.gridded.renderer.RasterizerGriddedElevationModelLayerRenderer;
import com.revolsys.swing.map.layer.record.style.panel.BaseStylePanel;
import com.revolsys.util.Property;

public class GriddedElevationModelStylePanel extends BaseStylePanel
  implements PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  private final transient GriddedElevationModelRasterizer rasterizer;

  public GriddedElevationModelStylePanel(
    final RasterizerGriddedElevationModelLayerRenderer renderer) {
    super(renderer, false);
    this.rasterizer = renderer.getRasterizer();
    final JPanel panel = new JPanel(new BorderLayout());
    add(panel, 1);
    final JPanel stylePanels = new JPanel(new VerticalLayout(5));
    panel.add(stylePanels, BorderLayout.CENTER);
    addPanel(this, "Opacity", renderer, "opacity");
    if (this.rasterizer instanceof ColorGriddedElevationModelRasterizer) {
      addPanel(this, "Style", this.rasterizer, "minZ", "maxZ", "minColour", "maxColour");
    } else if (this.rasterizer instanceof HillShadeGriddedElevationModelRasterizer) {
      addPanel(this, "Style", this.rasterizer, "azimuthDegrees", "zenithDegrees", "zFactor");
    } else if (this.rasterizer instanceof ColorGradientGriddedElevationModelRasterizer) {
      addPanel(this, "Style", this.rasterizer, "gradient");
    }
  }

  @Override
  protected Field newField(final String fieldName, final Class<?> fieldClass, final Object value) {
    if ("opacity".equals(fieldName)) {
      final PercentSlider opacityField = new PercentSlider("opacity", (float)value);
      Property.addListener(opacityField, this);
      opacityField.setMaximumSize(new Dimension(100, 25));
      return opacityField;
    } else if ("gradient".equals(fieldName)) {
      final ColorGradientField field = new ColorGradientField(
        (ColorGradientGriddedElevationModelRasterizer)this.rasterizer);
      Property.addListener(field, this);
      return field;
    } else {
      return super.newField(fieldName, fieldClass, value);
    }
  }

  @Override
  public void propertyChangeDo(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      if (this.rasterizer != null) {
        final String fieldName = field.getFieldName();
        final Object fieldValue = field.getFieldValue();
        this.rasterizer.setProperty(fieldName, fieldValue);
      }
    } else if (source == this.rasterizer) {
      final String name = event.getPropertyName();
      final Object value = event.getNewValue();
      setFieldValue(name, value);
    }
  }

  @Override
  public void save() {
    final RasterizerGriddedElevationModelLayerRenderer renderer = getRenderer();
    renderer.setRasterizer(this.rasterizer);
    super.save();
  }
}
