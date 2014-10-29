package com.revolsys.swing.map.layer.record.style.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.measure.Measure;
import javax.measure.quantity.Length;
import javax.measure.unit.NonSI;
import javax.measure.unit.Unit;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jdesktop.swingx.VerticalLayout;

import com.revolsys.awt.WebColors;
import com.revolsys.swing.Icons;
import com.revolsys.swing.SwingUtil;
import com.revolsys.swing.action.I18nAction;
import com.revolsys.swing.component.TogglePanel;
import com.revolsys.swing.component.ValueField;
import com.revolsys.swing.field.CheckBox;
import com.revolsys.swing.field.ColorChooserField;
import com.revolsys.swing.field.ComboBox;
import com.revolsys.swing.field.Field;
import com.revolsys.swing.field.FontChooserField;
import com.revolsys.swing.field.InvokeMethodStringConverter;
import com.revolsys.swing.field.LengthMeasureTextField;
import com.revolsys.swing.field.TextField;
import com.revolsys.swing.layout.GroupLayoutUtil;
import com.revolsys.swing.map.MapPanel;
import com.revolsys.swing.map.component.MapScale;
import com.revolsys.swing.map.component.MarkerField;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerRenderer;
import com.revolsys.swing.map.layer.record.AbstractRecordLayer;
import com.revolsys.swing.map.layer.record.style.GeometryStyle;
import com.revolsys.swing.map.layer.record.style.LineCap;
import com.revolsys.swing.map.layer.record.style.LineJoin;
import com.revolsys.swing.map.layer.record.style.MarkerStyle;
import com.revolsys.util.CaseConverter;
import com.revolsys.util.Property;

public class BaseStylePanel extends ValueField implements
PropertyChangeListener {
  public static List<Action> getLineActions(final String type,
    final String... alignmentTypes) {
    final List<Action> actions = new ArrayList<Action>();
    for (final String alignmentType : alignmentTypes) {
      final String iconName = ("line_" + type + "_" + alignmentType).toLowerCase();
      final ImageIcon icon = Icons.getIcon(iconName);
      final String toolTip = CaseConverter.toCapitalizedWords(alignmentType
        + " " + type);
      final I18nAction action = new I18nAction(alignmentType, null, toolTip,
        icon);
      actions.add(action);
    }
    return actions;
  }

  public static List<Action> getTextAlignActions(final String... alignmentTypes) {
    final List<Action> actions = new ArrayList<Action>();
    for (final String alignmentType : alignmentTypes) {
      final I18nAction action = new I18nAction(alignmentType, null,
        CaseConverter.toCapitalizedWords(alignmentType),
        Icons.getIcon("text_align_" + alignmentType));
      actions.add(action);
    }
    return actions;
  }

  private static final long serialVersionUID = 1L;

  public static final List<Action> HORIZONTAL_ALIGNMENT_ACTIONS = getTextAlignActions(
    "left", "center", "right");

  public static final List<Action> VERTICAL_ALIGNMENT_ACTIONS = getTextAlignActions(
    "top", "middle", "bottom");

  public static final List<Action> LINE_JOIN_ACTIONS = getLineActions("join",
    "MITER", "ROUND", "BEVEL");

  public static final List<Action> LINE_CAP_ACTIONS = getLineActions("cap",
    "BUTT", "ROUND", "SQUARE");

  private final Set<String> readOnlyFieldNames = new HashSet<String>();

  private final Set<String> rendererFieldNames = new HashSet<String>();

  private Field visibleField;

  public BaseStylePanel(final LayerRenderer<?> renderer) {
    super(renderer);
    setTitle("Style");
    setBackground(WebColors.White);
    setLayout(new VerticalLayout());
    addReadOnlyFieldName("type");
    Property.addListener(renderer, "visible", this);

    addPanel(this, "General", renderer, "name", "type", "visible");
    addPanel(this, "Scales", renderer, "minimumScale", "maximumScale");
    addPanel(this, "Filter", renderer, "queryFilter");
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

  protected Field addField(final JPanel container, final Object object,
    final String fieldName) {
    final Class<?> fieldClass = Property.getClass(object, fieldName);
    if (fieldClass == null) {
      return null;
    } else {
      final Object value = Property.get(object, fieldName);
      SwingUtil.addLabel(container, fieldName);
      final Field field = createField(fieldName, fieldClass, value);

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
    Property.addListener(field, fieldName, this);
    container.add(field);
  }

  protected void addLineStylePanel(final JPanel stylePanels,
    final GeometryStyle geometryStyle) {
    final JPanel panel = new JPanel();
    panel.setMinimumSize(new Dimension(300, 0));
    SwingUtil.setTitledBorder(panel, "Line Style");
    addField(panel, geometryStyle, "lineColor");
    addLengthMeasureField(panel, geometryStyle, "lineWidth");
    addField(panel, geometryStyle, "lineJoin");
    addField(panel, geometryStyle, "lineCap");
    addField(panel, geometryStyle, "lineDashArray");
    addField(panel, geometryStyle, "lineDashOffset");
    GroupLayoutUtil.makeColumns(panel, 2, true);
    stylePanels.add(panel);
  }

  protected void addMarkerStylePanel(final JPanel stylePanels,
    final MarkerStyle markerStyle) {
    addPanel(stylePanels, "Marker Style", markerStyle, "markerLineColor",
      "markerLineWidth", "markerFill", "markerWidth", "markerHeight", "marker");

    addPanel(stylePanels, "Marker Position", markerStyle,
      "markerHorizontalAlignment", "markerVerticalAlignment", "markerDx",
      "markerDy", "markerOrientationType", "markerOrientation",
        "markerPlacementType");

  }

  protected JPanel addPanel(final Container container, final String title,
    final Object object, final String... fieldNames) {
    final JPanel panel = new JPanel();
    SwingUtil.setTitledBorder(panel, title);

    addFields(panel, object, fieldNames);
    GroupLayoutUtil.makeColumns(panel, 2, true);
    container.add(panel);
    return panel;
  }

  protected void addPolygonStylePanel(final JPanel stylePanels,
    final GeometryStyle geometryStyle) {
    addPanel(stylePanels, "Polygon Style", geometryStyle, "polygonFill");
  }

  public void addReadOnlyFieldName(final String fieldName) {
    this.readOnlyFieldNames.add(fieldName);
  }

  protected void addTextField(final JPanel container, final Object object,
    final String fieldName, final int columns) {
    SwingUtil.addLabel(container, fieldName);
    final TextField field = new TextField(fieldName, columns);
    field.setFieldValue(Property.get(object, fieldName));
    Property.addListener(field, fieldName, this);
    container.add(field);
  }

  @SuppressWarnings("unchecked")
  protected Field createField(final String fieldName,
    final Class<?> fieldClass, final Object value) {
    Field field;
    if (fieldName.equals("visible")) {
      this.visibleField = new CheckBox(fieldName, value);
      field = this.visibleField;
    } else if (fieldName.equals("textFaceName")) {
      field = new FontChooserField(fieldName, (String)value);
    } else if (fieldName.endsWith("HorizontalAlignment")) {
      field = createHorizontalAlignmentField(fieldName, (String)value);
    } else if (fieldName.endsWith("VerticalAlignment")) {
      field = createVerticalAlignmentField(fieldName, (String)value);
    } else if (fieldName.equals("lineCap")) {
      field = createLineCapField((LineCap)value);
    } else if (fieldName.equals("lineJoin")) {
      field = createLineJoinField((LineJoin)value);
    } else if (fieldName.equals("lineDashArray")) {
      field = new DashField(fieldName, (List<Measure<Length>>)value);
    } else if (fieldName.equals("queryFilter")) {
      final AbstractRecordLayer layer = getLayer();
      field = new QueryFilterField(layer, fieldName, (String)value);
      field.setFieldValue(value);
      Property.addListener(field, fieldName, this);
    } else if (fieldName.equals("marker")) {
      field = new MarkerField(fieldName, value);
    } else if (fieldName.endsWith("OrientationType")) {
      final ComboBox orientationTypeField = new ComboBox(fieldName,
        new DefaultComboBoxModel<>(new String[] {
          "auto", "none"
        }));
      field = orientationTypeField;
    } else if (fieldName.endsWith("PlacementType")) {
      final ComboBox placementField = new ComboBox(fieldName,
        new DefaultComboBoxModel<>(new String[] {
          "auto", "center", "point(0)", "point(n)", "vertices"
        }));
      placementField.setFieldValue(value);
      field = placementField;
    } else if (fieldName.endsWith("Scale")) {
      field = createScaleField(fieldName, (Long)value);
    } else if (Color.class.equals(fieldClass)) {
      field = new ColorChooserField(fieldName, (Color)value);
    } else if (Boolean.TYPE.equals(fieldClass)
        || Boolean.class.equals(fieldClass)) {
      field = new CheckBox(fieldName, value);
    } else if (Measure.class.equals(fieldClass)) {
      field = new LengthMeasureTextField(fieldName, (Measure<Length>)value,
        NonSI.PIXEL);
    } else {
      field = new TextField(fieldName, value, 20);
    }
    return field;
  }

  protected TogglePanel createHorizontalAlignmentField(final String fieldName,
    String aligment) {
    if (!"left".equalsIgnoreCase(aligment)
        && !"right".equalsIgnoreCase(aligment)) {
      aligment = "center";
    }
    return new TogglePanel(fieldName, aligment, new Dimension(28, 28),
      HORIZONTAL_ALIGNMENT_ACTIONS);
  }

  protected TogglePanel createLineCapField(final LineCap lineCap) {
    return new TogglePanel("lineCap", lineCap.toString(),
      new Dimension(28, 28), LINE_CAP_ACTIONS);

  }

  protected TogglePanel createLineJoinField(final LineJoin lineJoin) {
    return new TogglePanel("lineJoin", lineJoin.toString(), new Dimension(28,
      28), LINE_JOIN_ACTIONS);
  }

  private Field createScaleField(final String fieldName, final Long value) {
    final Vector<Long> scales = new Vector<Long>();
    scales.add(Long.MAX_VALUE);
    scales.addAll(MapPanel.SCALES);
    final InvokeMethodStringConverter converter = new InvokeMethodStringConverter(
      MapScale.class, "formatScale");
    converter.setHorizontalAlignment(JLabel.RIGHT);
    final ComboBox field = new ComboBox(fieldName, new DefaultComboBoxModel(
      scales), converter, converter);
    ((JTextField)field.getEditor().getEditorComponent()).setHorizontalAlignment(JTextField.RIGHT);
    field.setSelectedItem(value);
    field.setPreferredSize(new Dimension(150, 22));
    return field;
  }

  protected TogglePanel createVerticalAlignmentField(final String fieldName,
    String aligment) {
    if (!"top".equalsIgnoreCase(aligment)
        && !"middle".equalsIgnoreCase(aligment)) {
      aligment = "bottom";
    }
    return new TogglePanel(fieldName, aligment, new Dimension(28, 28),
      VERTICAL_ALIGNMENT_ACTIONS);
  }

  protected void doPropertyChange(final PropertyChangeEvent event) {
  }

  @SuppressWarnings("unchecked")
  public <L extends Layer> L getLayer() {
    final LayerRenderer<Layer> renderer = getRenderer();
    return (L)renderer.getLayer();
  }

  @SuppressWarnings("unchecked")
  public <T extends LayerRenderer<Layer>> T getRenderer() {
    return (T)getFieldValue();
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    if (event.getSource() == getRenderer()) {
      if ("visible".equals(event.getPropertyName())) {
        this.visibleField.setFieldValue(event.getNewValue());
      }
    }
    if (!rendererPropertyChange(event)) {
      doPropertyChange(event);
    }
  }

  protected boolean rendererPropertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source instanceof Field) {
      final LayerRenderer<Layer> renderer = getRenderer();
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
        Property.set(renderer, fieldName, scale);
        return true;
      } else if (this.rendererFieldNames.contains(fieldName)) {
        Property.set(renderer, fieldName, fieldValue);
      }
    }
    return false;
  }

}
