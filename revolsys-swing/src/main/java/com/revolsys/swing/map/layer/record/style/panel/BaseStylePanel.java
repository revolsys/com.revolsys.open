package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.measure.Quantity;
import javax.measure.Unit;
import javax.measure.quantity.Length;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.jdesktop.swingx.VerticalLayout;
import org.jeometry.coordinatesystem.model.unit.CustomUnits;

import com.revolsys.geometry.model.LineCap;
import com.revolsys.geometry.model.LineJoin;
import com.revolsys.swing.Icons;
import com.revolsys.swing.Panels;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.Form;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.field.ColorChooserField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.FontChooserField;
import com.revolsys.swing.field.FunctionStringConverter;
import com.revolsys.swing.field.LengthMeasureTextField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayouts;
import com.revolsys.swing.map.Viewport2D;
import com.revolsys.swing.map.component.MapScale;
import com.revolsys.swing.map.component.MarkerField;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.swing.map.layer.record.style.TextStyle;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;
import com.revolsys.util.PropertyDescriptorCache;

public class BaseStylePanel extends Form implements PropertyChangeListener {
  public static final List<Action> HORIZONTAL_ALIGNMENT_ACTIONS = getTextAlignActions("left",
    "center", "right");

  public static final List<Action> LINE_CAP_ACTIONS = getLineActions("cap", "BUTT", "ROUND",
    "SQUARE");

  public static final List<Action> LINE_JOIN_ACTIONS = getLineActions("join", "MITER", "ROUND",
    "BEVEL");

  private static final long serialVersionUID = 1L;

  public static final List<Action> VERTICAL_ALIGNMENT_ACTIONS = getTextAlignActions("top", "middle",
    "bottom");

  public static List<Action> getLineActions(final String type, final String... alignmentTypes) {
    final List<Action> actions = new ArrayList<>();
    for (final String alignmentType : alignmentTypes) {
      final String iconName = ("line_" + type + "_" + alignmentType).toLowerCase();
      final Icon icon = Icons.getIcon(iconName);
      final String toolTip = CaseConverter.toCapitalizedWords(alignmentType + " " + type);
      final I18nAction action = new I18nAction(alignmentType, null, toolTip, icon);
      actions.add(action);
    }
    return actions;
  }

  public static List<Action> getTextAlignActions(final String... alignmentTypes) {
    final List<Action> actions = new ArrayList<>();
    for (final String alignmentType : alignmentTypes) {
      final I18nAction action = new I18nAction(alignmentType, null,
        CaseConverter.toCapitalizedWords(alignmentType),
        Icons.getIcon("text_align_" + alignmentType));
      actions.add(action);
    }
    return actions;
  }

  private final Set<String> readOnlyFieldNames = new HashSet<>();

  private final Set<String> rendererFieldNames = new HashSet<>();

  private Field visibleField;

  private final LayerRenderer<?> renderer;

  public BaseStylePanel(final LayerRenderer<?> renderer, final boolean showScaleFields) {
    super(new VerticalLayout());
    this.renderer = renderer;
    addReadOnlyFieldName("type");
    Property.addListener(renderer, this);
    addPanel(this, "General", renderer, "name", "type", "visible");
    if (showScaleFields) {
      addPanel(this, "Scales", renderer, "minimumScale", "maximumScale");
    }
    if (PropertyDescriptorCache.getPropertyDescriptor(renderer, "queryFilter") != null) {
      addPanel(this, "Filter", renderer, "queryFilter");
    }
  }

  protected void addCheckBoxField(final JPanel container, final Object object,
    final String fieldName) {
    SwingUtil.addLabel(container, fieldName);
    final CheckBox field = new CheckBox(fieldName);
    final Object value = Property.get(object, fieldName);
    field.setFieldValue(value);
    Property.addListener(field, fieldName, this);
    container.add(field);
  }

  protected void addColorField(final JPanel container, final Object object,
    final String fieldName) {
    SwingUtil.addLabel(container, fieldName);
    final Color value = Property.get(object, fieldName);
    final ColorChooserField field = new ColorChooserField(fieldName, value);
    Property.addListener(field, fieldName, this);
    container.add(field);
  }

  protected Field addField(final JPanel container, final Object object, final String fieldName) {
    final Class<?> fieldClass = Property.getClass(object, fieldName);
    if (fieldClass == null) {
      return null;
    } else {
      final Object value = Property.get(object, fieldName);
      SwingUtil.addLabel(container, fieldName);
      final Field field = newField(fieldName, fieldClass, value);
      setField(field);
      if (this.readOnlyFieldNames.contains(fieldName)) {
        field.setEditable(false);
      }
      if (field instanceof JTextArea) {
        container.add(new JScrollPane((Component)field));
      } else {
        container.add((Component)field);
      }
      Property.addListener(field, "fieldValue", this);
      Property.addListener(field, fieldName, this);
      if (object instanceof LayerRenderer) {
        this.rendererFieldNames.add(fieldName);
      }
      return field;
    }
  }

  protected void addFields(final JPanel container, final Object object,
    final List<String> fieldNames) {
    for (final String fieldName : fieldNames) {
      addField(container, object, fieldName);
    }
  }

  protected void addFields(final JPanel container, final Object object,
    final String... fieldNames) {
    addFields(container, object, Arrays.asList(fieldNames));
  }

  protected void addLengthMeasureField(final JPanel container, final Object object,
    final String fieldName) {
    SwingUtil.addLabel(container, fieldName);
    final Quantity<Length> value = Property.get(object, fieldName);
    Unit<Length> unit;
    if (value == null) {
      unit = CustomUnits.PIXEL;
    } else {
      unit = value.getUnit();
    }
    final LengthMeasureTextField field = new LengthMeasureTextField(fieldName, value, unit);
    Property.addListener(field, fieldName, this);
    container.add(field);
  }

  protected void addLineStylePanel(final JPanel stylePanels, final GeometryStyle geometryStyle) {
    final JPanel panel = Panels.titledTransparent("Line Style");
    panel.setMinimumSize(new Dimension(300, 0));
    addField(panel, geometryStyle, "lineColor");
    addLengthMeasureField(panel, geometryStyle, "lineWidth");
    addField(panel, geometryStyle, "lineJoin");
    addField(panel, geometryStyle, "lineCap");
    addField(panel, geometryStyle, "lineDashArray");
    addField(panel, geometryStyle, "lineDashOffset");
    GroupLayouts.makeColumns(panel, 2, true);
    stylePanels.add(panel);
  }

  protected void addMarkerStylePanel(final JPanel stylePanels, final MarkerStyle markerStyle) {
    addPanel(stylePanels, "Marker Style", markerStyle, "markerLineColor", "markerLineWidth",
      "markerFill", "markerWidth", "markerHeight", "marker");

    addPanel(stylePanels, "Marker Position", markerStyle, "markerHorizontalAlignment",
      "markerVerticalAlignment", "markerDx", "markerDy", "markerOrientationType",
      "markerOrientation", "markerPlacementType");

  }

  protected JPanel addPanel(final Container container, final String title, final Object object,
    final List<String> fieldNames) {
    final JPanel panel = Panels.titledTransparent(title);

    addFields(panel, object, fieldNames);
    GroupLayouts.makeColumns(panel, 2, true);
    container.add(panel);
    return panel;
  }

  protected JPanel addPanel(final Container container, final String title, final Object object,
    final String... fieldNames) {
    return addPanel(container, title, object, Arrays.asList(fieldNames));
  }

  protected void addPolygonStylePanel(final JPanel stylePanels, final GeometryStyle geometryStyle) {
    addPanel(stylePanels, "Polygon Style", geometryStyle, "polygonFill");
  }

  public void addReadOnlyFieldName(final String fieldName) {
    this.readOnlyFieldNames.add(fieldName);
  }

  protected void addTextField(final JPanel container, final Object object, final String fieldName,
    final int columns) {
    SwingUtil.addLabel(container, fieldName);
    final TextField field = new TextField(fieldName, columns);
    field.setFieldValue(Property.get(object, fieldName));
    Property.addListener(field, fieldName, this);
    container.add(field);
  }

  protected void addTextStylePanels(final JPanel stylePanels, final TextStyle textStyle) {
    addPanel(stylePanels, "Text Label", textStyle, "textName", "textSize", "textFaceName");
    addPanel(stylePanels, "Text Color", textStyle, "textFill", "textBoxColor", "textHaloFill",
      "textHaloRadius");
    addPanel(stylePanels, "Text Position", textStyle, "textHorizontalAlignment",
      "textVerticalAlignment", "textDx", "textDy", "textOrientationType", "textOrientation",
      "textPlacementType");
  }

  @SuppressWarnings("unchecked")
  public <L extends Layer> L getLayer() {
    final LayerRenderer<?> renderer = getRenderer();
    return (L)renderer.getLayer();
  }

  @SuppressWarnings("unchecked")
  public <L extends Layer, R extends LayerRenderer<L>> R getRenderer() {
    return (R)this.renderer;
  }

  @SuppressWarnings("unchecked")
  protected Field newField(final String fieldName, final Class<?> fieldClass, final Object value) {
    Field field;
    if (fieldName.equals("visible")) {
      this.visibleField = new CheckBox(fieldName, value);
      field = this.visibleField;
    } else if (fieldName.equals("textFaceName")) {
      field = new FontChooserField(fieldName, (String)value);
    } else if (fieldName.endsWith("HorizontalAlignment")) {
      field = newHorizontalAlignmentField(fieldName, (String)value);
    } else if (fieldName.endsWith("VerticalAlignment")) {
      field = newVerticalAlignmentField(fieldName, (String)value);
    } else if (fieldName.equals("lineCap")) {
      field = newLineCapField((LineCap)value);
    } else if (fieldName.equals("lineJoin")) {
      field = newLineJoinField((LineJoin)value);
    } else if (fieldName.equals("lineDashArray")) {
      field = new DashField(fieldName, (List<Quantity<Length>>)value);
    } else if (fieldName.equals("queryFilter")) {
      final AbstractRecordLayer layer = getLayer();
      field = new QueryFilterField(layer, fieldName, (String)value);
      field.setFieldValue(value);
      Property.addListener(field, fieldName, this);
    } else if (fieldName.equals("marker")) {
      field = new MarkerField(fieldName, value);
    } else if (fieldName.endsWith("OrientationType")) {
      final ComboBox<String> orientationTypeField = ComboBox.newComboBox(fieldName, "auto", "none");
      orientationTypeField.setFieldValue(value);
      field = orientationTypeField;
    } else if (fieldName.equals("markerPlacementType")) {
      final ComboBox<String> placementField = ComboBox.newComboBox(fieldName, "auto", "center",
        "vertex(0)", "vertex(n)", "vertices", "segment(0)", "segment(n)", "segments");
      placementField.setFieldValue(value);
      field = placementField;
    } else if (fieldName.equals("textPlacementType")) {
      final ComboBox<String> placementField = ComboBox.newComboBox(fieldName, "auto", "center",
        "vertex(0)", "vertex(n)", "segment(0)", "segment(n)");
      placementField.setFieldValue(value);
      field = placementField;
    } else if (fieldName.endsWith("Scale")) {
      field = newScaleField(fieldName, (Long)value);
    } else if (Color.class.equals(fieldClass)) {
      field = new ColorChooserField(fieldName, (Color)value);
    } else if (Boolean.TYPE.equals(fieldClass) || Boolean.class.equals(fieldClass)) {
      field = new CheckBox(fieldName, value);
    } else if (Quantity.class.equals(fieldClass)) {
      field = new LengthMeasureTextField(fieldName, (Quantity<Length>)value, CustomUnits.PIXEL);
    } else {
      field = new TextField(fieldName, value, 40);
    }
    return field;
  }

  protected TogglePanel newHorizontalAlignmentField(final String fieldName, String aligment) {
    if (!"left".equalsIgnoreCase(aligment) && !"right".equalsIgnoreCase(aligment)) {
      aligment = "center";
    }
    return new TogglePanel(fieldName, aligment, new Dimension(28, 28),
      HORIZONTAL_ALIGNMENT_ACTIONS);
  }

  protected TogglePanel newLineCapField(final LineCap lineCap) {
    return new TogglePanel("lineCap", lineCap.toString(), new Dimension(28, 28), LINE_CAP_ACTIONS);

  }

  protected TogglePanel newLineJoinField(final LineJoin lineJoin) {
    return new TogglePanel("lineJoin", lineJoin.toString(), new Dimension(28, 28),
      LINE_JOIN_ACTIONS);
  }

  private Field newScaleField(final String fieldName, final Long value) {
    final List<Long> scales = new ArrayList<>();
    scales.add(Long.MAX_VALUE);
    scales.addAll(Viewport2D.SCALES);
    final ComboBox<Long> field = ComboBox.newComboBox(fieldName, scales, MapScale::formatScale);
    ((FunctionStringConverter<?>)field.getRenderer()).setHorizontalAlignment(SwingConstants.RIGHT);
    ((JTextField)field.getEditor().getEditorComponent())
      .setHorizontalAlignment(SwingConstants.RIGHT);
    field.setSelectedItem(value);
    field.setPreferredSize(new Dimension(150, 22));
    return field;
  }

  protected TogglePanel newVerticalAlignmentField(final String fieldName, String aligment) {
    if (!"top".equalsIgnoreCase(aligment) && !"middle".equalsIgnoreCase(aligment)) {
      aligment = "bottom";
    }
    return new TogglePanel(fieldName, aligment, new Dimension(28, 28), VERTICAL_ALIGNMENT_ACTIONS);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getSource() == getRenderer()) {
      if ("visible".equals(event.getPropertyName())) {
        this.visibleField.setFieldValue(event.getNewValue());
      }
    }
    if (!rendererPropertyChange(event)) {
      propertyChangeDo(event);
    }
  }

  protected void propertyChangeDo(final PropertyChangeEvent event) {
  }

  protected boolean rendererPropertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final LayerRenderer<?> renderer = getRenderer();
      final Field field = (Field)source;
      final String fieldName = field.getFieldName();
      final Object fieldValue = field.getFieldValue();
      if (fieldName.endsWith("Scale")) {
        long scale = 0;
        if (fieldValue instanceof Number) {
          final Number number = (Number)fieldValue;
          scale = number.longValue();
        }
        if ("minimumScale".equals(fieldName)) {
          if (scale <= 0) {
            scale = Long.MAX_VALUE;
          }
        } else if ("maximumScale".equals(fieldName)) {
          if (scale == Long.MAX_VALUE) {
            scale = 0;
          }
        }
        renderer.setProperty(fieldName, scale);
        return true;
      } else if (this.rendererFieldNames.contains(fieldName)) {
        renderer.setProperty(fieldName, fieldValue);
      }
    }
    return false;
  }

}
