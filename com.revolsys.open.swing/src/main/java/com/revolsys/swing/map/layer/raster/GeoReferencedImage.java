package com.revolsys.swing.map.layer.raster;

import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.media.jai.PlanarImage;

import org.springframework.core.io.Resource;

import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;

public interface GeoReferencedImage extends MapSerializer,
  PropertyChangeListener {

  void cancelChanges();

  AffineTransform getAffineTransformation(BoundingBox boundingBox);

  BoundingBox getBoundingBox();

  CoordinateSystem getCoordinateSystem();

  int[] getDpi();

  GeometryFactory getGeometryFactory();

  BufferedImage getImage();

  GeoReferencedImage getImage(final CoordinateSystem coordinateSystem);

  GeoReferencedImage getImage(final CoordinateSystem coordinateSystem,
    final double resolution);

  GeoReferencedImage getImage(final GeometryFactory geometryFactory);

  double getImageAspectRatio();

  int getImageHeight();

  Resource getImageResource();

  int getImageWidth();

  PlanarImage getJaiImage();

  BufferedImage getOriginalImage();

  WarpFilter getOriginalWarpFilter();

  double getResolution();

  List<MappedLocation> getTiePoints();

  BufferedImage getWarpedImage();

  WarpFilter getWarpFilter();

  String getWorldFileExtension();

  boolean hasBoundingBox();

  boolean hasGeometryFactory();

  boolean isHasChanages();

  boolean saveChanges();

  void setBoundingBox(final BoundingBox boundingBox);

  void setBoundingBox(final double x1, final double y1,
    final double pixelWidth, final double pixelHeight);

  void setCoordinateSystem(final CoordinateSystem coordinateSystem);

  void setDpi(final int... dpi);

  void setGeometryFactory(final GeometryFactory geometryFactory);

  void setImage(final BufferedImage image);

  void setTiePoints(final List<MappedLocation> tiePoints);
}
