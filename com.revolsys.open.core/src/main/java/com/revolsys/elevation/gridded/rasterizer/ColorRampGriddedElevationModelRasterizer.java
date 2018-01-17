package com.revolsys.elevation.gridded.rasterizer;

import java.awt.Color;
import java.util.Map;

import com.revolsys.awt.gradient.GradientStop;
import com.revolsys.awt.gradient.LinearGradient;
import com.revolsys.awt.gradient.MultiStopLinearGradient;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;

public class ColorRampGriddedElevationModelRasterizer
  extends AbstractGriddedElevationModelRasterizer {
  private static final MultiStopLinearGradient SD_A_COLOR_RAMP = new MultiStopLinearGradient(//
    new GradientStop(0, new Color(84, 229, 151)), //
    new GradientStop(0.0209, new Color(84, 229, 151)), //
    new GradientStop(0.0209, new Color(97, 240, 130)), //
    new GradientStop(0.0418, new Color(97, 240, 130)), //
    new GradientStop(0.0418, new Color(115, 247, 117)), //
    new GradientStop(0.0626, new Color(115, 247, 117)), //
    new GradientStop(0.0626, new Color(148, 254, 133)), //
    new GradientStop(0.0835, new Color(148, 254, 133)), //
    new GradientStop(0.0835, new Color(158, 254, 135)), //
    new GradientStop(0.1044, new Color(158, 254, 135)), //
    new GradientStop(0.1044, new Color(173, 253, 136)), //
    new GradientStop(0.1253, new Color(173, 253, 136)), //
    new GradientStop(0.1253, new Color(180, 254, 139)), //
    new GradientStop(0.1462, new Color(180, 254, 139)), //
    new GradientStop(0.1462, new Color(189, 254, 140)), //
    new GradientStop(0.1671, new Color(189, 254, 140)), //
    new GradientStop(0.1671, new Color(197, 253, 141)), //
    new GradientStop(0.1879, new Color(197, 253, 141)), //
    new GradientStop(0.1879, new Color(207, 254, 144)), //
    new GradientStop(0.2088, new Color(207, 254, 144)), //
    new GradientStop(0.2088, new Color(215, 254, 146)), //
    new GradientStop(0.2297, new Color(215, 254, 146)), //
    new GradientStop(0.2297, new Color(223, 254, 147)), //
    new GradientStop(0.2506, new Color(223, 254, 147)), //
    new GradientStop(0.2506, new Color(231, 254, 149)), //
    new GradientStop(0.2715, new Color(231, 254, 149)), //
    new GradientStop(0.2715, new Color(238, 253, 151)), //
    new GradientStop(0.2923, new Color(238, 253, 151)), //
    new GradientStop(0.2923, new Color(247, 254, 154)), //
    new GradientStop(0.3132, new Color(247, 254, 154)), //
    new GradientStop(0.3132, new Color(254, 254, 155)), //
    new GradientStop(0.3341, new Color(254, 254, 155)), //
    new GradientStop(0.3341, new Color(253, 252, 151)), //
    new GradientStop(0.355, new Color(253, 252, 151)), //
    new GradientStop(0.355, new Color(254, 248, 149)), //
    new GradientStop(0.3759, new Color(254, 248, 149)), //
    new GradientStop(0.3759, new Color(253, 242, 146)), //
    new GradientStop(0.3968, new Color(253, 242, 146)), //
    new GradientStop(0.3968, new Color(254, 238, 146)), //
    new GradientStop(0.4176, new Color(254, 238, 146)), //
    new GradientStop(0.4176, new Color(253, 233, 140)), //
    new GradientStop(0.4385, new Color(253, 233, 140)), //
    new GradientStop(0.4385, new Color(254, 225, 135)), //
    new GradientStop(0.4594, new Color(254, 225, 135)), //
    new GradientStop(0.4594, new Color(253, 219, 127)), //
    new GradientStop(0.4803, new Color(253, 219, 127)), //
    new GradientStop(0.4803, new Color(254, 215, 121)), //
    new GradientStop(0.5012, new Color(254, 215, 121)), //
    new GradientStop(0.5012, new Color(253, 206, 125)), //
    new GradientStop(0.522, new Color(253, 206, 125)), //
    new GradientStop(0.522, new Color(250, 203, 128)), //
    new GradientStop(0.5429, new Color(250, 203, 128)), //
    new GradientStop(0.5429, new Color(248, 197, 131)), //
    new GradientStop(0.5638, new Color(248, 197, 131)), //
    new GradientStop(0.5638, new Color(246, 197, 134)), //
    new GradientStop(0.5847, new Color(246, 197, 134)), //
    new GradientStop(0.5847, new Color(244, 197, 136)), //
    new GradientStop(0.6056, new Color(244, 197, 136)), //
    new GradientStop(0.6056, new Color(242, 190, 139)), //
    new GradientStop(0.6265, new Color(242, 190, 139)), //
    new GradientStop(0.6265, new Color(240, 188, 143)), //
    new GradientStop(0.6473, new Color(240, 188, 143)), //
    new GradientStop(0.6473, new Color(238, 187, 145)), //
    new GradientStop(0.6682, new Color(238, 187, 145)), //
    new GradientStop(0.6682, new Color(235, 188, 153)), //
    new GradientStop(0.6891, new Color(235, 188, 153)), //
    new GradientStop(0.6891, new Color(235, 194, 164)), //
    new GradientStop(0.71, new Color(235, 194, 164)), //
    new GradientStop(0.71, new Color(232, 197, 172)), //
    new GradientStop(0.7309, new Color(232, 197, 172)), //
    new GradientStop(0.7309, new Color(230, 200, 177)), //
    new GradientStop(0.7517, new Color(230, 200, 177)), //
    new GradientStop(0.7517, new Color(223, 198, 179)), //
    new GradientStop(0.7726, new Color(223, 198, 179)), //
    new GradientStop(0.7726, new Color(221, 199, 183)), //
    new GradientStop(0.7935, new Color(221, 199, 183)), //
    new GradientStop(0.7935, new Color(224, 207, 194)), //
    new GradientStop(0.8144, new Color(224, 207, 194)), //
    new GradientStop(0.8144, new Color(228, 214, 203)), //
    new GradientStop(0.8353, new Color(228, 214, 203)), //
    new GradientStop(0.8353, new Color(232, 221, 212)), //
    new GradientStop(0.8561, new Color(232, 221, 212)), //
    new GradientStop(0.8561, new Color(235, 226, 219)), //
    new GradientStop(0.877, new Color(235, 226, 219)), //
    new GradientStop(0.877, new Color(239, 231, 225)), //
    new GradientStop(0.8979, new Color(239, 231, 225)), //
    new GradientStop(0.8979, new Color(243, 238, 234)), //
    new GradientStop(0.9188, new Color(243, 238, 234)), //
    new GradientStop(0.9188, new Color(246, 240, 236)), //
    new GradientStop(0.9397, new Color(246, 240, 236)), //
    new GradientStop(0.9397, new Color(250, 249, 248)), //
    new GradientStop(0.9606, new Color(250, 249, 248)), //
    new GradientStop(0.9606, new Color(255, 255, 255)), //
    new GradientStop(1, new Color(255, 255, 255)) //
  );

  private LinearGradient gradient;

  public ColorRampGriddedElevationModelRasterizer() {
    super("colorRampGriddedElevationModelRasterizer", "style_color_ramp");
    setGradient(SD_A_COLOR_RAMP);
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
  public ColorRampGriddedElevationModelRasterizer clone() {
    final ColorRampGriddedElevationModelRasterizer clone = (ColorRampGriddedElevationModelRasterizer)super.clone();
    clone.gradient = this.gradient.clone();
    return clone;
  }

  public LinearGradient getGradient() {
    return this.gradient;
  }

  @Override
  public String getName() {
    return "Color Ramp";
  }

  @Override
  public int getValue(final int gridX, final int gridY) {
    final double elevation = this.elevationModel.getElevation(gridX, gridY);
    return this.gradient.getColorIntForValue(elevation);

  }

  public void setGradient(final LinearGradient gradient) {
    this.gradient = gradient;
    updateValues();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    addToMap(map, "gradient", this.gradient);
    return map;
  }

  @Override
  public void updateValues() {
    this.gradient.setValueMin(this.minZ);
    this.gradient.setValueMax(this.maxZ);
    this.gradient.updateValues();
    firePropertyChange("styleUpdated", false, true);
  }
}
