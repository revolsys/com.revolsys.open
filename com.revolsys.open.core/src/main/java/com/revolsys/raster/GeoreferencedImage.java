package com.revolsys.raster;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import java.beans.PropertyChangeListener;
import java.util.List;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.spring.resource.Resource;

public interface GeoreferencedImage extends MapSerializer, PropertyChangeListener {

  void cancelChanges();

  void deleteTiePoint(MappedLocation tiePoint);

  void drawImage(Graphics2D graphics, BoundingBox viewBoundingBox, int viewWidth, int viewHeight,
    boolean useTransform);

  AffineTransform getAffineTransformation(BoundingBox boundingBox);

  BoundingBox getBoundingBox();

  CoordinateSystem getCoordinateSystem();

  int[] getDpi();

  GeometryFactory getGeometryFactory();

  GeoreferencedImage getImage(final CoordinateSystem coordinateSystem);

  GeoreferencedImage getImage(final CoordinateSystem coordinateSystem, final double resolution);

  GeoreferencedImage getImage(final GeometryFactory geometryFactory);

  double getImageAspectRatio();

  int getImageHeight();

  Resource getImageResource();

  int getImageWidth();

  List<Dimension> getOverviewSizes();

  RenderedImage getRenderedImage();

  double getResolution();

  List<MappedLocation> getTiePoints();

  String getWorldFileExtension();

  boolean hasBoundingBox();

  boolean hasGeometryFactory();

  boolean isHasChanages();

  boolean isHasTransform();

  boolean saveChanges();

  void setBoundingBox(final BoundingBox boundingBox);

  void setBoundingBox(final double x1, final double y1, final double pixelWidth,
    final double pixelHeight);

  void setDpi(final int... dpi);

  void setRenderedImage(final RenderedImage image);

  void setTiePoints(final List<MappedLocation> tiePoints);
}
