package com.revolsys.swing.map.layer.dataobject.style;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.util.CaseConverter;

public enum MarkerStyleProperty {
  /** */
  marker_file("marker-file"),
  /** */
  marker_opacity("marker-opacity", DataTypes.DOUBLE),
  /** */
  marker_fill_opacity("marker-fill-opacity", DataTypes.DOUBLE),
  /** */
  marker_line_color("marker-line-color", DataTypes.COLOR),
  /** */
  marker_line_width("marker-line-width", DataTypes.DOUBLE),
  /** */
  marker_line_opacity("marker-line-opacity", DataTypes.DOUBLE),
  /** */
  marker_placement("marker-placement"),
  /** */
  marker_type("marker-type"),
  /** */
  marker_width("marker-width", DataTypes.DOUBLE),
  /** */
  marker_height("marker-height", DataTypes.DOUBLE),
  /** */
  marker_fill("marker-fill", DataTypes.COLOR),
  /** */
  marker_allow_overlap("marker-allow-overlap", DataTypes.BOOLEAN),
  /** */
  marker_ignore_placement("marker-ignore-placement"),
  /** */
  marker_spacing("marker-spacing"),
  /** */
  marker_max_error("marker-max-error"),
  /** */
  marker_transform("marker-transform"),
  /** */
  marker_clip("marker-clip", DataTypes.BOOLEAN),
  /** */
  marker_smooth("marker-smooth", DataTypes.DOUBLE),
  /** */
  marker_comp_op("marker-comp-op"),
  // Custom
  /** */
  MARKER_ORIENTATION_TYPE("marker-orientation-type"),
  /** */
  MARKER_HORIZONTAL_ALIGNMENT("marker-horizontal-alignment"),
  /** */
  MARKER_VERTICAL_ALIGNMENT("marker-vertical-alignment"),
  /** */
  MARKER_DX("marker-dx"),
  /** */
  MARKER_DY("marker-dy");
  ;

  private static final Map<String, MarkerStyleProperty> propertiesByLabel = new LinkedHashMap<String, MarkerStyleProperty>();

  static {
    for (final MarkerStyleProperty property : MarkerStyleProperty.values()) {
      final String label = property.getLabel();
      propertiesByLabel.put(label, property);
    }
  }

  public static MarkerStyleProperty getProperty(final String label) {
    return propertiesByLabel.get(label);
  }

  private String label;

  private DataType dataType;

  private String propertyName;

  private MarkerStyleProperty(final String label) {
    this(label, DataTypes.STRING);
  }

  private MarkerStyleProperty(final String label, final DataType dataType) {
    this.label = label;
    this.dataType = dataType;
    this.propertyName = CaseConverter.toLowerCamelCase(name());
  }

  public DataType getDataType() {
    return dataType;
  }

  public String getLabel() {
    return label;
  }

  public String getPropertyName() {
    return propertyName;
  }

  @Override
  public String toString() {
    return label;
  }
}
