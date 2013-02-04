package com.revolsys.swing.map.layer.dataobject.style;

import java.util.LinkedHashMap;
import java.util.Map;

import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.util.CaseConverter;

public enum TextStyleProperty {
 
  /** */
  text_name("text-name"),
  /** */
  text_face_name("text-face-name"),
  /** */
  text_size("text-size", DataTypes.DOUBLE),
  /** */
  text_ratio("text-ratio"),
  /** */
  text_wrap_width("text-wrap-width", DataTypes.DOUBLE),
  /** */
  text_wrap_before("text-wrap-before"),
  /** */
  text_wrap_character("text-wrap-character"),
  /** */
  text_spacing("text-spacing"),
  /** */
  text_character_spacing("text-character-spacing"),
  /** */
  text_line_spacing("text-line-spacing"),
  /** */
  text_label_position_tolerance("text-label-position-tolerance"),
  /** */
  text_max_char_angle_delta("text-max-char-angle-delta"),
  /** */
  text_fill("text-fill", DataTypes.COLOR),
  /** */
  text_opacity("text-opacity"),
  /** */
  text_halo_fill("text-halo-fill", DataTypes.COLOR),
  /** */
  text_halo_radius("text-halo-radius", DataTypes.DOUBLE),
  /** */
  text_dx("text-dx", DataTypes.DOUBLE),
  /** */
  text_dy("text-dy", DataTypes.DOUBLE),
  /** */
  text_vertical_alignment("text-vertical-alignment"),
  /** */
  text_avoid_edges("text-avoid-edges"),
  /** */
  text_min_distance("text-min-distance"),
  /** */
  text_min_padding("text-min-padding"),
  /** */
  text_min_path_length("text-min-path-length"),
  /** */
  text_allow_overlap("text-allow-overlap"),
  /** */
  text_orientation("text-orientation"),
  /** */
  text_placement("text-placement"),
  /** */
  text_placement_type("text-placement-type"),
  /** */
  text_placements("text-placements"),
  /** */
  text_transform("text-transform"),
  /** */
  text_horizontal_alignment("text-horizontal-alignment"),
  /** */
  text_align("text-align"),
  /** */
  text_clip("text-clip"),
  /** */
  text_comp_op("text-comp-op")
  // Custom Extensions
  ;
 
  private static final Map<String, TextStyleProperty> propertiesByLabel = new LinkedHashMap<String, TextStyleProperty>();

  static {
    for (final TextStyleProperty property : TextStyleProperty.values()) {
      final String label = property.getLabel();
      propertiesByLabel.put(label, property);
    }
  }

  public static TextStyleProperty getProperty(final String label) {
    return propertiesByLabel.get(label);
  }

  private String label;

  private DataType dataType;

  private String propertyName;

  private TextStyleProperty(final String label) {
    this(label, DataTypes.STRING);
  }

  private TextStyleProperty(final String label, final DataType dataType) {
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
