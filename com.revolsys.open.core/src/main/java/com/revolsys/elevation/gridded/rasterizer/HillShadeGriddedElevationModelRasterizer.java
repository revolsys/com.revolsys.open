package com.revolsys.elevation.gridded.rasterizer;

import java.util.Map;

import com.revolsys.awt.WebColors;
import com.revolsys.collection.map.MapEx;
import com.revolsys.elevation.gridded.GriddedElevationModel;
import com.revolsys.util.MathUtil;

public class HillShadeGriddedElevationModelRasterizer
  extends AbstractGriddedElevationModelRasterizer {
  private static final double PI_TIMES_2_MINUS_PI_OVER_2 = MathUtil.PI_TIMES_2 - MathUtil.PI_OVER_2;

  private double zenithRadians;

  private double azimuthRadians;

  private double cosZenithRadians;

  private double sinZenithRadians;

  private double oneDivCellSizeTimes8;

  private double zFactor = 1;

  private double zenithDegrees;

  private double azimuthDegrees;

  public HillShadeGriddedElevationModelRasterizer() {
    super("hillShadeGriddedElevationModelRasterizer");
    setZenithDegrees(45.0);
    setAzimuthDegrees(315.0);
  }

  public HillShadeGriddedElevationModelRasterizer(final GriddedElevationModel elevationModel) {
    this();
    setElevationModel(elevationModel);
  }

  public HillShadeGriddedElevationModelRasterizer(final Map<String, ? extends Object> config) {
    this();
    setProperties(config);
  }

  public double getAzimuthDegrees() {
    return this.azimuthDegrees;
  }

  private int getHillShade(final double a, final double b, final double c, final double d,
    final double f, final double g, final double h, final double i) {
    final double oneDivCellSizeTimes8 = this.oneDivCellSizeTimes8;
    final double dzDivDx = (c + 2 * f + i - (a + 2 * d + g)) * oneDivCellSizeTimes8;
    final double dzDivDy = (g + 2 * h + i - (a + 2 * b + c)) * oneDivCellSizeTimes8;
    final double slopeRadians = Math
      .atan(this.zFactor * Math.sqrt(dzDivDx * dzDivDx + dzDivDy * dzDivDy));

    double aspectRadians;
    if (dzDivDx == 0) {
      if (dzDivDy > 0) {
        aspectRadians = MathUtil.PI_OVER_2;
      } else if (dzDivDy < 0) {
        aspectRadians = PI_TIMES_2_MINUS_PI_OVER_2;
      } else {
        aspectRadians = 0;
      }
    } else {
      aspectRadians = Math.atan2(dzDivDy, -dzDivDx);
      if (aspectRadians < 0) {
        aspectRadians = MathUtil.PI_TIMES_2 + aspectRadians;
      }

    }
    final int hillshade = (int)(255.0
      * (this.cosZenithRadians * Math.cos(slopeRadians) + this.sinZenithRadians
        * Math.sin(slopeRadians) * Math.cos(this.azimuthRadians - aspectRadians)));

    return WebColors.colorToRGB(255, hillshade, hillshade, hillshade);
  }

  @Override
  public String getName() {
    return "Hillshade";
  }

  @Override
  public int getValue(final int gridX, final int gridY) {
    final GriddedElevationModel elevationModel = this.elevationModel;
    final int width = this.width;
    final int height = this.height;

    double a = Double.NaN;
    double b = Double.NaN;
    double c = Double.NaN;
    double d = Double.NaN;
    final double e = elevationModel.getElevationFast(gridX, gridY);
    if (Double.isFinite(e)) {
      double f = Double.NaN;
      double g = Double.NaN;
      double h = Double.NaN;
      double i = Double.NaN;

      final boolean firstX = gridX == 0;
      final boolean firstY = gridY == 0;
      final boolean lastX = gridX == width - 1;
      final boolean lastY = gridY == height - 1;
      final int gridX0 = gridX - 1;
      final int gridX2 = gridX + 1;
      if (!lastY) {
        final int gridY2 = gridY + 1;
        if (!firstX) {
          a = elevationModel.getElevationFast(gridX0, gridY2);
        }
        b = elevationModel.getElevationFast(gridX, gridY2);
        if (!lastX) {
          c = elevationModel.getElevationFast(gridX2, gridY2);
        }
      }
      if (!firstX) {
        d = elevationModel.getElevationFast(gridX0, gridY);
      }
      if (!lastX) {
        f = elevationModel.getElevationFast(gridX2, gridY);
      }
      if (!firstY) {
        final int gridY0 = gridY - 1;
        if (!firstX) {
          g = elevationModel.getElevationFast(gridX0, gridY0);
        }
        h = elevationModel.getElevationFast(gridX, gridY0);
        if (!lastX) {
          i = elevationModel.getElevationFast(gridX2, gridY0);
        }
      }

      if (!Double.isFinite(d)) {
        if (Double.isFinite(f)) {
          d = e - (f - e);
        } else {
          d = e;
          f = e;
        }
      } else if (!Double.isFinite(f)) {
        f = e;
      }
      if (!Double.isFinite(a)) {
        if (Double.isFinite(g)) {
          a = d - (g - d);
        } else {
          a = d;
        }
      }
      if (!Double.isFinite(b)) {
        if (Double.isFinite(h)) {
          b = e - (h - e);
        } else {
          b = e;
        }
      }
      if (!Double.isFinite(c)) {
        if (Double.isFinite(i)) {
          c = f - (i - f);
        } else {
          c = f;
        }
      }
      if (!Double.isFinite(g)) {
        g = d - (a - d);
      }
      if (!Double.isFinite(h)) {
        h = e - (b - e);
      }
      if (!Double.isFinite(i)) {
        i = f - (c - f);
      }
      return getHillShade(a, b, c, d, f, g, h, i);
    } else {
      return GriddedElevationModel.NULL_COLOUR;
    }
  }

  public double getZenithDegrees() {
    return this.zenithDegrees;
  }

  public double getzFactor() {
    return this.zFactor;
  }

  public void setAzimuthDegrees(final double azimuthDegrees) {
    final double oldValue = this.azimuthDegrees;
    this.azimuthDegrees = azimuthDegrees;
    this.azimuthRadians = Math.toRadians(360 - azimuthDegrees + 90);
    firePropertyChange("azimuthDegrees", oldValue, azimuthDegrees);
  }

  @Override
  public void setElevationModel(final GriddedElevationModel elevationModel) {
    super.setElevationModel(elevationModel);
  }

  public void setZenithDegrees(final double zenithDegrees) {
    final double oldValue = this.zenithDegrees;
    this.zenithDegrees = zenithDegrees;
    this.zenithRadians = Math.toRadians(90 - zenithDegrees);
    this.cosZenithRadians = Math.cos(this.zenithRadians);
    this.sinZenithRadians = Math.sin(this.zenithRadians);
    firePropertyChange("zenithDegrees", oldValue, zenithDegrees);
  }

  public void setzFactor(final double zFactor) {
    final double oldValue = this.zFactor;
    this.zFactor = zFactor;
    firePropertyChange("zFactor", oldValue, zFactor);
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    map.put("azimuthDegrees", this.azimuthDegrees);
    map.put("zenithDegrees", this.zenithDegrees);
    map.put("zFactor", this.zFactor);
    return map;
  }

  @Override
  protected void updateValues() {
    if (this.elevationModel != null) {
      final double cellSize = this.elevationModel.getGridCellSize();
      this.oneDivCellSizeTimes8 = 1.0 / (8 * cellSize);
    }
  }
}
