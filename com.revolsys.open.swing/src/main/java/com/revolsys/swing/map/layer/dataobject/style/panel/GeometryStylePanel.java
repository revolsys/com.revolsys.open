package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.gis.data.model.Attribute;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.LengthMeasureTextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;
import com.revolsys.swing.map.layer.dataobject.renderer.GeometryStyleRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.util.JavaBeanUtil;

@SuppressWarnings("serial")
public class GeometryStylePanel extends ValueField implements
  PropertyChangeListener {

  private final GeometryStyleRenderer geometryStyleRenderer;

  private final GeometryStyle geometryStyle;

  private DataType geometryDataType;

  private JPanel previews;

  public GeometryStylePanel(final GeometryStyleRenderer geometryStyleRenderer) {
    setTitle("Style");
    this.geometryStyleRenderer = geometryStyleRenderer;
    this.geometryStyle = geometryStyleRenderer.getStyle().clone();
    final DataObjectLayer layer = geometryStyleRenderer.getLayer();
    final DataObjectMetaData metaData = layer.getMetaData();
    final Attribute geometryAttribute = metaData.getGeometryAttribute();
    setFieldValue(geometryStyleRenderer);
    if (geometryAttribute != null) {
      geometryDataType = geometryAttribute.getType();
      if (DataTypes.GEOMETRY_COLLECTION.equals(geometryDataType)) {
        geometryDataType = DataTypes.GEOMETRY;
      } else if (DataTypes.MULTI_POINT.equals(geometryDataType)) {
        geometryDataType = DataTypes.POINT;
      } else if (DataTypes.MULTI_LINE_STRING.equals(geometryDataType)) {
        geometryDataType = DataTypes.LINE_STRING;
      } else if (DataTypes.MULTI_POLYGON.equals(geometryDataType)) {
        geometryDataType = DataTypes.POLYGON;
      }

      final boolean hasLineStyle = false;
      final boolean hasPolygonStyle = false;

      final JPanel stylePanels = new JPanel();
      add(stylePanels);

      previews = new JPanel(new GridLayout(1, 1));
      previews.setBorder(BorderFactory.createTitledBorder("Preview"));
      add(previews);

      if (DataTypes.GEOMETRY.equals(geometryDataType)) {
        addMarkerStylePanel(stylePanels);
        addLineStylePanel(stylePanels);
        addPolygonStylePanel(stylePanels);
        previews.add(new MarkerStylePreview(geometryStyle));
        addGeometryPreview(DataTypes.LINE_STRING);
        addGeometryPreview(DataTypes.POLYGON);
      } else if (DataTypes.POINT.equals(geometryDataType)) {
        addMarkerStylePanel(stylePanels);
        previews.add(new MarkerStylePreview(geometryStyle));
      } else if (DataTypes.LINE_STRING.equals(geometryDataType)) {
        addLineStylePanel(stylePanels);
        addGeometryPreview(DataTypes.LINE_STRING);
      } else if (DataTypes.POLYGON.equals(geometryDataType)) {
        addLineStylePanel(stylePanels);
        addPolygonStylePanel(stylePanels);
        addGeometryPreview(DataTypes.POLYGON);
      }

      if (hasLineStyle) {
      }
      if (hasPolygonStyle) {
      }
      GroupLayoutUtil.makeColumns(stylePanels, 1);

      GroupLayoutUtil.makeColumns(this, 2);
    }
  }

  protected void addField(final JPanel panel, final String fieldName) {
    final String label = fieldName;
    addField(panel, fieldName, label);
  }

  @SuppressWarnings("unchecked")
  protected void addField(final JPanel panel, final String fieldName,
    final String label) {
    addLabel(panel, label);
    final Object fieldValue = JavaBeanUtil.getValue(geometryStyle, fieldName);
    JComponent field;
    if (fieldName.equals("lineCap")) {
      field = createLineCapField((String)fieldValue);
    } else if (fieldName.equals("lineJoin")) {
      field = createLineJoinField((String)fieldValue);
    } else if (fieldName.endsWith("Measure")) {
      field = createMeasureField(fieldName, (Measure<Length>)fieldValue);
    } else {
      field = SwingUtil.createField(fieldValue.getClass(), fieldName,
        fieldValue);
    }
    if (field instanceof Field) {
      field.addPropertyChangeListener(fieldName, this);
    } else {
      field.addPropertyChangeListener(this);
    }
    panel.add(field);
  }

  protected void addGeometryPreview(final DataType geometryDataType) {
    final GeometryStylePreview linePreview = new GeometryStylePreview(
      geometryStyle, geometryDataType);
    previews.add(linePreview);
  }

  protected void addLabel(final JPanel panel, final String text) {
    final JLabel label = SwingUtil.addLabel(panel, text);
    label.setMinimumSize(new Dimension(120, 10));
  }

  protected void addLineStylePanel(final JPanel stylePanels) {
    final JPanel panel = new JPanel();
    panel.setMinimumSize(new Dimension(300, 0));
    panel.setBorder(BorderFactory.createTitledBorder("Line Style"));
    addField(panel, "lineColor");
    addField(panel, "lineWidthMeasure", "Line Width");
    addField(panel, "lineJoin");
    addField(panel, "lineCap");
    GroupLayoutUtil.makeColumns(panel, 2);
    stylePanels.add(panel);
  }

  protected void addMarkerStylePanel(final JPanel stylePanels) {
    final JPanel panel = new JPanel();
    panel.setMinimumSize(new Dimension(300, 0));
    panel.setBorder(BorderFactory.createTitledBorder("Marker Style"));
    addField(panel, "markerLineColor");
    addField(panel, "markerLineWidthMeasure", "Line Width");
    addField(panel, "markerFill");
    GroupLayoutUtil.makeColumns(panel, 2);
    stylePanels.add(panel);
  }

  protected void addPolygonStylePanel(final JPanel stylePanels) {
    final JPanel panel = new JPanel();
    panel.setMinimumSize(new Dimension(300, 0));
    panel.setBorder(BorderFactory.createTitledBorder("Polygon Style"));

    addField(panel, "polygonFill");

    GroupLayoutUtil.makeColumns(panel, 2);
    stylePanels.add(panel);
  }

  protected TogglePanel createLineCapField(final String lineCap) {
    final I18nAction lineCapButtAction = new I18nAction("BUTT", null,
      "Butt Cap", SilkIconLoader.getIcon("line_cap_butt"));
    final I18nAction lineCapRoundAction = new I18nAction("ROUND", null,
      "Round Cap", SilkIconLoader.getIcon("line_cap_round"));
    final I18nAction lineCapSquareAction = new I18nAction("SQUARE", null,
      "Square Cap", SilkIconLoader.getIcon("line_cap_square"));
    return new TogglePanel("lineCap", lineCap, new Dimension(28, 28),
      lineCapButtAction, lineCapRoundAction, lineCapSquareAction);

  }

  protected TogglePanel createLineJoinField(final String lineJoin) {
    final I18nAction miterAction = new I18nAction("MITER", null, "miterJoin",
      SilkIconLoader.getIcon("line_join_miter"));
    final I18nAction roundAction = new I18nAction("ROUND", null, "roundJoin",
      SilkIconLoader.getIcon("line_join_round"));
    final I18nAction bevel = new I18nAction("BEVEL", null, "bevelJoin",
      SilkIconLoader.getIcon("line_join_bevel"));
    return new TogglePanel("lineJoin", lineJoin, new Dimension(28, 28),
      miterAction, roundAction, bevel);
  }

  protected LengthMeasureTextField createMeasureField(final String fieldName,
    final Measure<Length> measure) {
    final LengthMeasureTextField widthField = new LengthMeasureTextField(
      fieldName, measure);
    widthField.addPropertyChangeListener("number", this);
    widthField.addPropertyChangeListener("unit", this);
    return widthField;
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      JavaBeanUtil.setProperty(geometryStyle, fieldName, fieldValue);
    }
    for (final Component preview : previews.getComponents()) {
      preview.repaint();
    }
  }

  @Override
  public void save() {
    super.save();
    geometryStyleRenderer.setStyle(geometryStyle);
  }

  public void setLineWidthField(final ValueField panel,
    final JFormattedTextField field) {
    final Measure<Length> measure = panel.getFieldValue();
    if (measure == null) {
      field.setValue(0);
    } else {
      final Number value = measure.getValue();
      field.setValue(value.intValue());

    }
  }

  public void setLineWidthSliderValue(final ValueField panel,
    final JSlider slider) {
    final Measure<Length> measure = panel.getFieldValue();
    if (measure == null) {
      slider.setValue(0);
    } else {
      final Number value = measure.getValue();
      slider.setValue(value.intValue());
    }
  }

  public void setLineWidthTextFieldValue(final ValueField panel,
    final JFormattedTextField field) {
    final Measure<Length> measure = panel.getFieldValue();
    final Unit<Length> unit = measure.getUnit();
    final double fieldValue = ((Number)field.getValue()).doubleValue();
    panel.setFieldValue(Measure.valueOf(fieldValue, unit));

  }

  public void setLineWidthValue(final ValueField panel, final JSlider slider) {
    final Measure<Length> measure = panel.getFieldValue();
    Unit<Length> unit;
    if (measure == null) {
      unit = NonSI.PIXEL;
    } else {
      unit = measure.getUnit();
    }
    final int sliderValue = slider.getValue();
    panel.setFieldValue(Measure.valueOf((double)sliderValue, unit));

  }

}
