package com.revolsys.elevation.gridded;

import com.revolsys.awt.WebColors;
import com.revolsys.util.MathUtil;

public class HillShadeConfiguration {
  private static final double PI_TIMES_2_MINUS_PI_OVER_2 = MathUtil.PI_TIMES_2 - MathUtil.PI_OVER_2;

  private GriddedElevationModel elevationModel;

  private int width;

  private int height;

  private double zenithRadians;

  private double azimuthRadians;

  private double cosZenithRadians;

  private double sinZenithRadians;

  private double oneDivCellSizeTimes8;

  private final double zFactor = 1;

  public HillShadeConfiguration(final GriddedElevationModel elevationModel) {
    setElevationModel(elevationModel);

    setZenithDegrees(45.0);
    setAzimuthDegrees(315.0);
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

  public int getHillShade(final int index) {
    final int width = this.width;
    final int height = this.height;
    final int gridX = index % width;
    final int gridY = height - 1 - (index - gridX) / width;
    return getHillShade(gridX, gridY);
  }

  private int getHillShade(final int gridX, final int gridY) {
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

  private void setAzimuthDegrees(final double azimuth) {
    this.azimuthRadians = Math.toRadians(360 - azimuth + 90);
  }

  private void setElevationModel(final GriddedElevationModel elevationModel) {
    this.elevationModel = elevationModel;
    this.width = elevationModel.getGridWidth();
    this.height = elevationModel.getGridHeight();
    final int cellSize = elevationModel.getGridCellSize();
    this.oneDivCellSizeTimes8 = 1.0 / (8 * cellSize);
  }

  private void setZenithDegrees(final double zenith) {
    this.zenithRadians = Math.toRadians(90 - zenith);
    this.cosZenithRadians = Math.cos(this.zenithRadians);
    this.sinZenithRadians = Math.sin(this.zenithRadians);
  }
}
