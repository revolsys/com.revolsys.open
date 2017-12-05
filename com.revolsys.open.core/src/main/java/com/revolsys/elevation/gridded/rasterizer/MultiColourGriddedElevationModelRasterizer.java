package com.revolsys.elevation.gridded.rasterizer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;

public class MultiColourGriddedElevationModelRasterizer
  extends AbstractGriddedElevationModelRasterizer {

  private static final int NULL_COLOR = WebColors.colorToRGB(0, 0, 0, 0);

  private final List<ColorRange> colorRanges = Arrays.asList();

  public MultiColourGriddedElevationModelRasterizer() {
    super("colourGriddedElevationModelRasterizer");
    updateValues();
  }

  public MultiColourGriddedElevationModelRasterizer(final GriddedElevationModel elevationModel) {
    this();
    setElevationModel(elevationModel);
  }

  public MultiColourGriddedElevationModelRasterizer(final Map<String, ? extends Object> config) {
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
        if (colorRange.inRange(elevation)) {
          return colorRange.getValueFast(elevation);
        }
      }
      return this.colorRanges.get(this.colorRanges.size() - 1).getMaxColourInt();
    } else {
      return NULL_COLOR;
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
    for (final ColorRange colorRange : this.colorRanges) {
      colorRange.updateValues();
    }
  }
}
