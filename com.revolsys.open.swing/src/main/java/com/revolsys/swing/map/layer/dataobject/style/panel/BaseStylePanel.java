package com.revolsys.swing.map.layer.dataobject.style.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.famfamfam.silk.SilkIconLoader;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.field.ColorChooserField;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.FontChooserField;
import com.revolsys.swing.field.LengthMeasureTextField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.dataobject.style.GeometryStyle;
import com.revolsys.swing.map.layer.dataobject.style.LineCap;
import com.revolsys.swing.map.layer.dataobject.style.LineJoin;
import com.revolsys.swing.map.layer.dataobject.style.MarkerStyle;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class BaseStylePanel extends ValueField implements
  PropertyChangeListener {
  private static final long serialVersionUID = 1L;

  public static final List<Action> HORIZONTAL_ALIGNMENT_ACTIONS = getTextAlignActions(
    "left", "center", "right");

  public static final List<Action> VERTICAL_ALIGNMENT_ACTIONS = getTextAlignActions(
    "top", "middle", "bottom");

  public static final List<Action> LINE_JOIN_ACTIONS = getLineActions("join",
    "MITER", "ROUND", "BEVEL");

  public static final List<Action> LINE_CAP_ACTIONS = getLineActions("cap",
    "BUTT", "ROUND", "SQUARE");

  public static List<Action> getLineActions(final String type,
    final String... alignmentTypes) {
    final List<Action> actions = new ArrayList<Action>();
    for (final String alignmentType : alignmentTypes) {
      final I18nAction action = new I18nAction(alignmentType, null,
        CaseConverter.toCapitalizedWords(alignmentType + " " + type),
        SilkIconLoader.getIcon("line_" + type.toLowerCase() + "_"
          + alignmentType));
      actions.add(action);
    }
    return actions;
  }

  public static List<Action> getTextAlignActions(final String... alignmentTypes) {
    final List<Action> actions = new ArrayList<Action>();
    for (final String alignmentType : alignmentTypes) {
      final I18nAction action = new I18nAction(alignmentType, null,
        CaseConverter.toCapitalizedWords(alignmentType),
        SilkIconLoader.getIcon("text_align_" + alignmentType));
      actions.add(action);
    }
    return actions;
  }

  public BaseStylePanel(final LayerRenderer<?> renderer) {
    super(renderer);
    setTitle("Style");
    setLayout(new VerticalLayout());

    // TODO min/max scale
    addPanel(this, "General", renderer, "name", "visible");
  }

  protected void addCheckBoxField(final JPanel container, final Object object,
    final String fieldName) {
    SwingUtil.addLabel(container, fieldName);
    final CheckBox field = new CheckBox(fieldName);
    final Object value = Property.get(object, fieldName);
    field.setFieldValue(value);
    field.addPropertyChangeListener(fieldName, this);
    container.add(field);
  }

  protected void addColorField(final JPanel container, final Object object,
    final String fieldName) {
    SwingUtil.addLabel(container, fieldName);
    final Color value = Property.get(object, fieldName);
    final ColorChooserField field = new ColorChooserField(fieldName, value);
    field.addPropertyChangeListener(fieldName, this);
    container.add(field);
  }

  @SuppressWarnings("unchecked")
  protected Field addField(final JPanel container, final Object object,
    final String fieldName) {
    final Class<?> fieldClass = Property.getClass(object, fieldName);
    if (fieldClass == null) {
      return null;
    } else {
      final Object value = Property.get(object, fieldName);
      SwingUtil.addLabel(container, fieldName);
      Field field;
      if (fieldName.equals("textFaceName")) {
        field = new FontChooserField(fieldName, (String)value);
      } else if (fieldName.equals("textHorizontalAlignment")) {
        field = createHorizontalAlignmentField((String)value);
      } else if (fieldName.equals("textVerticalAlignment")) {
        field = createVerticalAlignmentField((String)value);
      } else if (fieldName.equals("lineCap")) {
        field = createLineCapField((LineCap)value);
      } else if (fieldName.equals("lineJoin")) {
        field = createLineJoinField((LineJoin)value);
      } else if (Color.class.equals(fieldClass)) {
        field = new ColorChooserField(fieldName, (Color)value);
      } else if (Boolean.TYPE.equals(fieldClass)
        || Boolean.class.equals(fieldClass)) {
        field = new CheckBox(fieldName, value);
      } else if (Measure.class.equals(fieldClass)) {
        field = new LengthMeasureTextField(fieldName, (Measure<Length>)value,
          NonSI.PIXEL);
      } else {
        field = new TextField(fieldName, value, 30);
      }
      container.add((Component)field);
      return field;
    }
  }

  protected void addFields(final JPanel container, final Object object,
    final String... fieldNames) {
    for (final String fieldName : fieldNames) {
      addField(container, object, fieldName);
    }
  }

  protected void addLengthMeasureField(final JPanel container,
    final Object object, final String fieldName) {
    SwingUtil.addLabel(container, fieldName);
    final Measure<Length> value = Property.get(object, fieldName);
    Unit<Length> unit;
    if (value == null) {
      unit = NonSI.PIXEL;
    } else {
      unit = value.getUnit();
    }
    final LengthMeasureTextField field = new LengthMeasureTextField(fieldName,
      value, unit);
    field.addPropertyChangeListener(fieldName, this);
    container.add(field);
  }

  protected void addLineStylePanel(final JPanel stylePanels,
    final GeometryStyle geometryStyle) {
    final JPanel panel = new JPanel();
    panel.setMinimumSize(new Dimension(300, 0));
    panel.setBorder(BorderFactory.createTitledBorder("Line Style"));
    addField(panel, geometryStyle, "lineColor");
    addLengthMeasureField(panel, geometryStyle, "lineWidth");
    addField(panel, geometryStyle, "lineJoin");
    addField(panel, geometryStyle, "lineCap");
    GroupLayoutUtil.makeColumns(panel, 2);
    stylePanels.add(panel);
  }

  protected void addMarkerStylePanel(final JPanel stylePanels,
    final MarkerStyle markerStyle) {
    addPanel(stylePanels, "Marker Style", markerStyle, "markerLineColor",
      "markerLineWidth", "markerFill");
  }

  protected JPanel addPanel(final Container container, final String title,
    final Object object, final String... fieldNames) {
    final JPanel panel = new JPanel();
    panel.setBorder(BorderFactory.createTitledBorder(title));
    addFields(panel, object, fieldNames);
    GroupLayoutUtil.makeColumns(panel, 2);
    container.add(panel);
    return panel;
  }

  protected void addPolygonStylePanel(final JPanel stylePanels,
    final GeometryStyle geometryStyle) {
    addPanel(stylePanels, "Polygon Style", geometryStyle, "polygonFill");
  }

  protected void addTextField(final JPanel container, final Object object,
    final String fieldName, final int columns) {
    SwingUtil.addLabel(container, fieldName);
    final TextField field = new TextField(fieldName, columns);
    field.setFieldValue(Property.get(object, fieldName));
    field.addPropertyChangeListener(fieldName, this);
    container.add(field);
  }

  protected TogglePanel createHorizontalAlignmentField(String aligment) {
    if (!"left".equalsIgnoreCase(aligment)
      && !"right".equalsIgnoreCase(aligment)) {
      aligment = "center";
    }
    return new TogglePanel("textHorizontalAlignment", aligment, new Dimension(
      28, 28), HORIZONTAL_ALIGNMENT_ACTIONS);
  }

  protected TogglePanel createLineCapField(final LineCap lineCap) {
    return new TogglePanel("lineCap", lineCap.toString(),
      new Dimension(28, 28), LINE_CAP_ACTIONS);

  }

  protected TogglePanel createLineJoinField(final LineJoin lineJoin) {
    return new TogglePanel("lineJoin", lineJoin.toString(), new Dimension(28,
      28), LINE_JOIN_ACTIONS);
  }

  protected TogglePanel createVerticalAlignmentField(String aligment) {
    if (!"top".equalsIgnoreCase(aligment)
      && !"bottom".equalsIgnoreCase(aligment)) {
      aligment = "middle";
    }
    return new TogglePanel("textVerticalAlignment", aligment, new Dimension(28,
      28), VERTICAL_ALIGNMENT_ACTIONS);
  }

  @SuppressWarnings("unchecked")
  public <T extends LayerRenderer<Layer>> T getRenderer() {
    return (T)getFieldValue();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent evt) {
  }
}
