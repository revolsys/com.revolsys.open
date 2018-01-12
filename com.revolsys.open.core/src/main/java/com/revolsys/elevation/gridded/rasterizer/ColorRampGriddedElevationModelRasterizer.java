package com.revolsys.elevation.gridded.rasterizer;

import java.awt.Color;
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

  private static final List<ColorRange> SD_A_COLOR_RAMP = Arrays.asList(//
    new ColorRange(0, new Color(84, 229, 151)), //
    new ColorRange(0.0209, new Color(84, 229, 151)), //
    new ColorRange(0.0209, new Color(97, 240, 130)), //
    new ColorRange(0.0418, new Color(97, 240, 130)), //
    new ColorRange(0.0418, new Color(115, 247, 117)), //
    new ColorRange(0.0626, new Color(115, 247, 117)), //
    new ColorRange(0.0626, new Color(148, 254, 133)), //
    new ColorRange(0.0835, new Color(148, 254, 133)), //
    new ColorRange(0.0835, new Color(158, 254, 135)), //
    new ColorRange(0.1044, new Color(158, 254, 135)), //
    new ColorRange(0.1044, new Color(173, 253, 136)), //
    new ColorRange(0.1253, new Color(173, 253, 136)), //
    new ColorRange(0.1253, new Color(180, 254, 139)), //
    new ColorRange(0.1462, new Color(180, 254, 139)), //
    new ColorRange(0.1462, new Color(189, 254, 140)), //
    new ColorRange(0.1671, new Color(189, 254, 140)), //
    new ColorRange(0.1671, new Color(197, 253, 141)), //
    new ColorRange(0.1879, new Color(197, 253, 141)), //
    new ColorRange(0.1879, new Color(207, 254, 144)), //
    new ColorRange(0.2088, new Color(207, 254, 144)), //
    new ColorRange(0.2088, new Color(215, 254, 146)), //
    new ColorRange(0.2297, new Color(215, 254, 146)), //
    new ColorRange(0.2297, new Color(223, 254, 147)), //
    new ColorRange(0.2506, new Color(223, 254, 147)), //
    new ColorRange(0.2506, new Color(231, 254, 149)), //
    new ColorRange(0.2715, new Color(231, 254, 149)), //
    new ColorRange(0.2715, new Color(238, 253, 151)), //
    new ColorRange(0.2923, new Color(238, 253, 151)), //
    new ColorRange(0.2923, new Color(247, 254, 154)), //
    new ColorRange(0.3132, new Color(247, 254, 154)), //
    new ColorRange(0.3132, new Color(254, 254, 155)), //
    new ColorRange(0.3341, new Color(254, 254, 155)), //
    new ColorRange(0.3341, new Color(253, 252, 151)), //
    new ColorRange(0.355, new Color(253, 252, 151)), //
    new ColorRange(0.355, new Color(254, 248, 149)), //
    new ColorRange(0.3759, new Color(254, 248, 149)), //
    new ColorRange(0.3759, new Color(253, 242, 146)), //
    new ColorRange(0.3968, new Color(253, 242, 146)), //
    new ColorRange(0.3968, new Color(254, 238, 146)), //
    new ColorRange(0.4176, new Color(254, 238, 146)), //
    new ColorRange(0.4176, new Color(253, 233, 140)), //
    new ColorRange(0.4385, new Color(253, 233, 140)), //
    new ColorRange(0.4385, new Color(254, 225, 135)), //
    new ColorRange(0.4594, new Color(254, 225, 135)), //
    new ColorRange(0.4594, new Color(253, 219, 127)), //
    new ColorRange(0.4803, new Color(253, 219, 127)), //
    new ColorRange(0.4803, new Color(254, 215, 121)), //
    new ColorRange(0.5012, new Color(254, 215, 121)), //
    new ColorRange(0.5012, new Color(253, 206, 125)), //
    new ColorRange(0.522, new Color(253, 206, 125)), //
    new ColorRange(0.522, new Color(250, 203, 128)), //
    new ColorRange(0.5429, new Color(250, 203, 128)), //
    new ColorRange(0.5429, new Color(248, 197, 131)), //
    new ColorRange(0.5638, new Color(248, 197, 131)), //
    new ColorRange(0.5638, new Color(246, 197, 134)), //
    new ColorRange(0.5847, new Color(246, 197, 134)), //
    new ColorRange(0.5847, new Color(244, 197, 136)), //
    new ColorRange(0.6056, new Color(244, 197, 136)), //
    new ColorRange(0.6056, new Color(242, 190, 139)), //
    new ColorRange(0.6265, new Color(242, 190, 139)), //
    new ColorRange(0.6265, new Color(240, 188, 143)), //
    new ColorRange(0.6473, new Color(240, 188, 143)), //
    new ColorRange(0.6473, new Color(238, 187, 145)), //
    new ColorRange(0.6682, new Color(238, 187, 145)), //
    new ColorRange(0.6682, new Color(235, 188, 153)), //
    new ColorRange(0.6891, new Color(235, 188, 153)), //
    new ColorRange(0.6891, new Color(235, 194, 164)), //
    new ColorRange(0.71, new Color(235, 194, 164)), //
    new ColorRange(0.71, new Color(232, 197, 172)), //
    new ColorRange(0.7309, new Color(232, 197, 172)), //
    new ColorRange(0.7309, new Color(230, 200, 177)), //
    new ColorRange(0.7517, new Color(230, 200, 177)), //
    new ColorRange(0.7517, new Color(223, 198, 179)), //
    new ColorRange(0.7726, new Color(223, 198, 179)), //
    new ColorRange(0.7726, new Color(221, 199, 183)), //
    new ColorRange(0.7935, new Color(221, 199, 183)), //
    new ColorRange(0.7935, new Color(224, 207, 194)), //
    new ColorRange(0.8144, new Color(224, 207, 194)), //
    new ColorRange(0.8144, new Color(228, 214, 203)), //
    new ColorRange(0.8353, new Color(228, 214, 203)), //
    new ColorRange(0.8353, new Color(232, 221, 212)), //
    new ColorRange(0.8561, new Color(232, 221, 212)), //
    new ColorRange(0.8561, new Color(235, 226, 219)), //
    new ColorRange(0.877, new Color(235, 226, 219)), //
    new ColorRange(0.877, new Color(239, 231, 225)), //
    new ColorRange(0.8979, new Color(239, 231, 225)), //
    new ColorRange(0.8979, new Color(243, 238, 234)), //
    new ColorRange(0.9188, new Color(243, 238, 234)), //
    new ColorRange(0.9188, new Color(246, 240, 236)), //
    new ColorRange(0.9397, new Color(246, 240, 236)), //
    new ColorRange(0.9397, new Color(250, 249, 248)), //
    new ColorRange(0.9606, new Color(250, 249, 248)), //
    new ColorRange(0.9606, new Color(255, 255, 255)), //
    new ColorRange(1, new Color(255, 255, 255)) //
  );

  private List<ColorRange> colorRanges = new ArrayList<>();

  public ColorRampGriddedElevationModelRasterizer() {
    super("colorRampGriddedElevationModelRasterizer", "style_color_ramp");
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
  protected ColorRampGriddedElevationModelRasterizer clone() {
    final ColorRampGriddedElevationModelRasterizer clone = (ColorRampGriddedElevationModelRasterizer)super.clone();
    clone.colorRanges = new ArrayList<>();
    for (final ColorRange colorRange : this.colorRanges) {
      clone.colorRanges.add(colorRange.clone());
    }
    return clone;
  }

  public List<ColorRange> getColorRanges() {
    return this.colorRanges;
  }

  @Override
  public String getName() {
    return "Color Ramp";
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
  public void updateValues() {
    super.updateValues();
    Collections.sort(this.colorRanges);
    for (int i = 0; i < this.colorRanges.size() - 1; i++) {
      final ColorRange range = this.colorRanges.get(i);
      final ColorRange rangeNext = this.colorRanges.get(i + 1);
      final double percent = range.getPercent();
      final double nextPercent = rangeNext.getPercent();
      final double minZ = this.minZ + this.rangeZ * percent;
      final double maxZ = this.minZ + this.rangeZ * nextPercent;
      range.setMinMaxZ(minZ, maxZ);
    }
    firePropertyChange("styleUpdated", false, true);
  }
}
