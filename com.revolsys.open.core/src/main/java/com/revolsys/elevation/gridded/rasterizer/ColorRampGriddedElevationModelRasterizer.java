package com.revolsys.elevation.gridded.rasterizer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.util.Property;

public class ColorRampGriddedElevationModelRasterizer
  extends AbstractGriddedElevationModelRasterizer {

  private static final int NULL_COLOR = WebColors.colorToRGB(0, 0, 0, 0);

  private static final List<ColorRange> SD_A_COLOR_RAMP = Arrays.asList(
    new ColorRange(45.82, "#54e597"), new ColorRange(90.62, "#61f082"),
    new ColorRange(135.4, "#73f775"), new ColorRange(180, "#94fe85"),
    new ColorRange(224.8, "#9efe87"), new ColorRange(269.6, "#adfd88"),
    new ColorRange(314.4, "#b4fe8b"), new ColorRange(359.2, "#bdfe8c"),
    new ColorRange(404, "#c5fd8d"), new ColorRange(448.6, "#cffe90"),
    new ColorRange(493.4, "#d7fe92"), new ColorRange(538.2, "#dffe93"),
    new ColorRange(583, "#e7fe95"), new ColorRange(627.8, "#eefd97"),
    new ColorRange(672.4, "#f7fe9a"), new ColorRange(717.2, "#fefe9b"),
    new ColorRange(762, "#fdfc97"), new ColorRange(806.8, "#fef895"),
    new ColorRange(851.6, "#fdf292"), new ColorRange(896.4, "#feee92"),
    new ColorRange(941, "#fde98c"), new ColorRange(985.8, "#fee187"),
    new ColorRange(1031, "#fddb7f"), new ColorRange(1075, "#fed779"),
    new ColorRange(1120, "#fdce7d"), new ColorRange(1165, "#facb80"),
    new ColorRange(1210, "#f8c583"), new ColorRange(1254, "#f6c586"),
    new ColorRange(1299, "#f4c588"), new ColorRange(1344, "#f2be8b"),
    new ColorRange(1389, "#f0bc8f"), new ColorRange(1433, "#eebb91"),
    new ColorRange(1478, "#ebbc99"), new ColorRange(1523, "#ebc2a4"),
    new ColorRange(1568, "#e8c5ac"), new ColorRange(1613, "#e6c8b1"),
    new ColorRange(1657, "#dfc6b3"), new ColorRange(1702, "#ddc7b7"),
    new ColorRange(1747, "#e0cfc2"), new ColorRange(1792, "#e4d6cb"),
    new ColorRange(1836, "#e8ddd4"), new ColorRange(1881, "#ebe2db"),
    new ColorRange(1926, "#efe7e1"), new ColorRange(1971, "#f3eeea"),
    new ColorRange(2015, "#f6f0ec"), new ColorRange(2060, "#faf9f8"),
    new ColorRange(2105, "#ffffff"), new ColorRange(2190, "#ffffff"));

  private final List<ColorRange> colorRanges = new ArrayList<>();

  public ColorRampGriddedElevationModelRasterizer() {
    super("colourRampGriddedElevationModelRasterizer");
    setColorRanges(SD_A_COLOR_RAMP);
    updateValues();
  }

  public ColorRampGriddedElevationModelRasterizer(final GriddedElevationModel elevationModel) {
    this();
    setElevationModel(elevationModel);
  }

  public ColorRampGriddedElevationModelRasterizer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  @Override
  public String getName() {
    return "Multi Colour";
  }

  @Override
  public int getValue(final int gridX, final int gridY) {
    final double elevation = this.elevationModel.getElevation(gridX, gridY);
    if (Double.isFinite(elevation)) {
      for (final ColorRange colorRange : this.colorRanges) {
        final int color = colorRange.getValueFast(elevation);
        if (color != NULL_COLOR) {
          return color;
        }
      }
      return this.colorRanges.get(this.colorRanges.size() - 1).getMaxColourInt();
    } else {
      return NULL_COLOR;
    }
  }

  public void setColorRanges(final List<ColorRange> colorRanges) {
    if (Property.hasValue(colorRanges)) {
      this.colorRanges.clear();
      for (final ColorRange colorRange : colorRanges) {
        this.colorRanges.add(colorRange.clone());
      }
      updateValues();
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "colorRanges", this.colorRanges);

    return map;
  }

  @Override
  protected void updateValues() {
    Collections.sort(this.colorRanges);
    for (int i = 0; i < this.colorRanges.size() - 1; i++) {
      final ColorRange range = this.colorRanges.get(i);
      final ColorRange rangeNext = this.colorRanges.get(i + 1);
      final double maxZ = rangeNext.getMaxZ();
      range.setMaxZ(maxZ);
    }
  }
}
