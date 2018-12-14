package com.revolsys.swing.map.layer.elevation;

import java.util.List;

import com.revolsys.geometry.model.Point;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.swing.map.layer.Layer;
import com.revolsys.swing.map.layer.LayerGroup;

public interface ElevationModelLayer extends Layer {

  static double getElevation(final LayerGroup layerGroup, final double scale, final Point point) {
    if (layerGroup.isVisible(scale)) {
      for (final Layer layer : layerGroup) {
        if (layer instanceof LayerGroup) {
          final LayerGroup childGroup = (LayerGroup)layer;
          final double elevation = getElevation(childGroup, scale, point);
          if (Double.isFinite(elevation)) {
            return elevation;
          }
        } else if (layer instanceof ElevationModelLayer) {
          final ElevationModelLayer elevationModel = (ElevationModelLayer)layer;
          if (elevationModel.isUseElevationAtScale(scale)) {
            final double elevation = elevationModel.getElevation(point);
            if (Double.isFinite(elevation)) {
              return elevation;
            }
          }
        }
      }
    }
    return Double.NaN;
  }

  static double getElevation(final List<ElevationModelLayer> layers, final Point point) {
    for (final ElevationModelLayer layer : layers) {
      final double elevation = layer.getElevation(point);
      if (Double.isFinite(elevation)) {
        return elevation;
      }
    }
    return Double.NaN;
  }

  static double getElevationVisible(final LayerGroup layerGroup, final double scale,
    final Point point) {
    if (layerGroup.isVisible(scale)) {
      for (final Layer layer : layerGroup) {
        if (layer instanceof LayerGroup) {
          final LayerGroup childGroup = (LayerGroup)layer;
          final double elevation = getElevation(childGroup, scale, point);
          if (Double.isFinite(elevation)) {
            return elevation;
          }
        } else if (layer instanceof ElevationModelLayer) {
          final ElevationModelLayer elevationModel = (ElevationModelLayer)layer;
          if (elevationModel.isVisible(scale)) {
            final double elevation = elevationModel.getElevation(point);
            if (Double.isFinite(elevation)) {
              return elevation;
            }
          }
        }
      }
    }
    return Double.NaN;
  }

  static List<ElevationModelLayer> getVisibleLayers(final LayerGroup layerGroup,
    final double scale) {
    return layerGroup.getVisibleDescendants(ElevationModelLayer.class, scale);
  }

  double getElevation(final double x, double y);

  default double getElevation(final Point point) {
    final Point convertedPoint = convertGeometry(point);
    final double x = convertedPoint.getX();
    final double y = convertedPoint.getY();
    return getElevation(x, y);
  }

  default boolean isUseElevationAtScale(final double scale) {
    return isVisible(scale);
  }

  BufferedGeoreferencedImage newRenderImage();

  void redraw();
}
