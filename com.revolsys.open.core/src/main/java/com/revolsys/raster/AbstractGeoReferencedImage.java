package com.revolsys.raster;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.revolsys.beans.AbstractPropertyChangeObject;
import com.revolsys.collection.PropertyChangeArrayList;
import com.revolsys.data.equals.EqualsRegistry;
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.io.xml.DomUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.impl.BoundingBoxDoubleGf;
import com.revolsys.jts.geom.impl.PointDouble;
import com.revolsys.spring.SpringUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public abstract class AbstractGeoReferencedImage extends
  AbstractPropertyChangeObject implements GeoReferencedImage {

  private static double[] calculateLSM(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight,
    final List<MappedLocation> mappings) {

    final GeneralMatrix A = getAMatrix(mappings, imageHeight);

    final GeneralMatrix X = getXMatrix(boundingBox, imageWidth, imageHeight,
      mappings);

    final GeneralMatrix P = getWeights(mappings.size());

    final GeneralMatrix AT = A.clone();
    AT.transpose();

    final GeneralMatrix ATP = new GeneralMatrix(AT.getNumRow(), P.getNumCol());
    final GeneralMatrix ATPA = new GeneralMatrix(AT.getNumRow(), A.getNumCol());
    final GeneralMatrix ATPX = new GeneralMatrix(AT.getNumRow(), 1);
    final GeneralMatrix x = new GeneralMatrix(A.getNumCol(), 1);
    ATP.mul(AT, P);
    ATPA.mul(ATP, A);
    ATPX.mul(ATP, X);
    ATPA.invert();
    x.mul(ATPA, ATPX);
    ATPA.invert();

    x.transpose();

    return x.getElements()[0];
  }

  public static GeneralMatrix getAMatrix(final List<MappedLocation> mappings,
    final int imageHeight) {
    final GeneralMatrix A = new GeneralMatrix(2 * mappings.size(), 6);

    final int numRow = mappings.size() * 2;

    for (int j = 0; j < numRow / 2; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Point sourcePoint = mappedLocation.getSourcePixel();
      final double x = sourcePoint.getX();
      final double y = imageHeight - sourcePoint.getY();
      A.setRowValues(j, x, y, 1.0D, 0.0D, 0.0D, 0.0D);
    }

    for (int j = numRow / 2; j < numRow; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - numRow / 2);
      final Point sourcePoint = mappedLocation.getSourcePixel();
      final double x = sourcePoint.getX();
      final double y = imageHeight - sourcePoint.getY();
      A.setRowValues(j, 0.0D, 0.0D, 0.0D, x, y, 1.0D);
    }
    return A;
  }

  public static int[] getResolution(final ImageReader r) throws IOException {
    int hdpi = 96, vdpi = 96;
    final double mm2inch = 25.4;

    NodeList lst;
    final Element node = (Element)r.getImageMetadata(0).getAsTree(
        "javax_imageio_1.0");
    lst = node.getElementsByTagName("HorizontalPixelSize");
    if (lst != null && lst.getLength() == 1) {
      hdpi = (int)(mm2inch / Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));
    }

    lst = node.getElementsByTagName("VerticalPixelSize");
    if (lst != null && lst.getLength() == 1) {
      vdpi = (int)(mm2inch / Float.parseFloat(((Element)lst.item(0)).getAttribute("value")));
    }

    return new int[] {
      hdpi, vdpi
    };
  }

  public static GeneralMatrix getWeights(final int size) {
    final GeneralMatrix P = new GeneralMatrix(size * 2, size * 2);

    for (int j = 0; j < size; ++j) {
      P.setElement(j, j, 1.0D);
    }
    return P;
  }

  private static GeneralMatrix getXMatrix(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight,
    final List<MappedLocation> mappings) {
    final GeneralMatrix X = new GeneralMatrix(2 * mappings.size(), 1);

    final int numRow = X.getNumRow();

    for (int j = 0; j < numRow / 2; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Point targetPixel = mappedLocation.getTargetPixel(boundingBox,
        imageWidth, imageHeight);
      final double x = targetPixel.getX();
      X.setElement(j, 0, x);
    }

    for (int j = numRow / 2; j < numRow; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - numRow / 2);
      final Point targetPixel = mappedLocation.getTargetPixel(boundingBox,
        imageWidth, imageHeight);
      final double y = imageHeight - targetPixel.getY();
      X.setElement(j, 0, y);
    }
    return X;
  }

  private List<Dimension> overviewSizes = new ArrayList<>();

  private BoundingBox boundingBox = new BoundingBoxDoubleGf();

  private int imageWidth = -1;

  private int imageHeight = -1;

  private GeometryFactory geometryFactory = GeometryFactory.floating3();

  private RenderedImage renderedImage;

  private Resource imageResource;

  private double resolution;

  private final Map<CoordinateSystem, AbstractGeoReferencedImage> projectedImages = new HashMap<CoordinateSystem, AbstractGeoReferencedImage>();

  private final PropertyChangeArrayList<MappedLocation> tiePoints = new PropertyChangeArrayList<MappedLocation>();

  private boolean hasChanges;

  private int[] dpi;

  private File file;

  public AbstractGeoReferencedImage() {
  }

  protected void addOverviewSize(final int width, final int height) {
    final Dimension size = new Dimension(width, height);
    this.overviewSizes.add(size);
  }

  @Override
  public void cancelChanges() {
    if (this.imageResource != null) {
      loadImageMetaData();
      setHasChanges(false);
    }
  }

  @Override
  public void drawImage(final Graphics2D graphics,
    final BoundingBox viewBoundingBox, final int viewWidth,
    final int viewHeight, final boolean useTransform) {
    final BoundingBox imageBoundingBox = getBoundingBox();
    if (viewBoundingBox.intersects(imageBoundingBox) && viewWidth > 0
      && viewHeight > 0) {
      final RenderedImage renderedImage = getRenderedImage();
      drawRenderedImage(renderedImage, graphics, viewBoundingBox, viewWidth,
        viewHeight, useTransform);
    }
  }

  public void drawRenderedImage(final RenderedImage renderedImage,
    final BoundingBox imageBoundingBox, final Graphics2D graphics,
    final BoundingBox viewBoundingBox, final int viewWidth,
    final boolean useTransform) {
    if (renderedImage != null) {
      final int imageWidth = renderedImage.getWidth();
      final int imageHeight = renderedImage.getHeight();
      if (imageWidth > 0 && imageHeight > 0) {

        final AffineTransform transform = graphics.getTransform();
        try {
          final double scaleFactor = viewWidth / viewBoundingBox.getWidth();

          final double imageMinX = imageBoundingBox.getMinX();
          final double viewMinX = viewBoundingBox.getMinX();
          final double screenX = (imageMinX - viewMinX) * scaleFactor;

          final double imageMaxY = imageBoundingBox.getMaxY();
          final double viewMaxY = viewBoundingBox.getMaxY();
          final double screenY = -(imageMaxY - viewMaxY) * scaleFactor;

          final double imageModelWidth = imageBoundingBox.getWidth();
          final int imageScreenWidth = (int)Math.ceil(imageModelWidth
            * scaleFactor);

          final double imageModelHeight = imageBoundingBox.getHeight();
          final int imageScreenHeight = (int)Math.ceil(imageModelHeight
            * scaleFactor);

          if (imageScreenWidth > 0 && imageScreenWidth < 10000
            && imageScreenHeight > 0 && imageScreenHeight < 10000) {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (imageScreenWidth > 0 && imageScreenHeight > 0) {

              graphics.translate(screenX, screenY);
              if (renderedImage instanceof BufferedImage && !useTransform) {
                final BufferedImage bufferedImage = (BufferedImage)renderedImage;
                try {
                  graphics.drawImage(bufferedImage, 0, 0, imageScreenWidth,
                    imageScreenHeight, null);
                } catch (final Throwable e) {
                  LoggerFactory.getLogger(getClass()).error(
                    imageScreenWidth + "x" + imageScreenHeight, e);
                }
              } else {
                final double scaleX = (double)imageScreenWidth / imageWidth;
                final double scaleY = (double)imageScreenHeight / imageHeight;
                final AffineTransform imageTransform = new AffineTransform(
                  scaleX, 0, 0, scaleY, 0, 0);
                if (useTransform) {
                  final AffineTransform geoTransform = getAffineTransformation(imageBoundingBox);
                  imageTransform.concatenate(geoTransform);
                }
                graphics.drawRenderedImage(renderedImage, imageTransform);
              }
            }
          }
        } catch (final Throwable e) {
          e.printStackTrace();
        } finally {
          graphics.setTransform(transform);
        }
      }
    }
  }

  protected void drawRenderedImage(final RenderedImage renderedImage,
    final Graphics2D graphics, final BoundingBox viewBoundingBox,
    final int viewWidth, final int viewHeight, final boolean useTransform) {
    final BoundingBox imageBoundingBox = getBoundingBox();
    drawRenderedImage(renderedImage, imageBoundingBox, graphics,
      viewBoundingBox, viewWidth, useTransform);
  }

  @Override
  public AffineTransform getAffineTransformation(final BoundingBox boundingBox) {
    final List<MappedLocation> mappings = getTiePoints();
    if (mappings.size() < 3) {
      return new AffineTransform();
    } else {
      final double[] affineTransformMatrix = calculateLSM(boundingBox,
        this.imageWidth, this.imageHeight, mappings);
      final double translateX = affineTransformMatrix[2];
      final double translateY = affineTransformMatrix[5];
      final double scaleX = affineTransformMatrix[0];
      final double scaleY = affineTransformMatrix[4];
      final double shearX = affineTransformMatrix[1];
      final double shearY = affineTransformMatrix[3];
      return new AffineTransform(scaleX, shearY, shearX, scaleY, translateX,
        translateY);
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  protected BufferedImage getBufferedImage() {
    final RenderedImage renderedImage = getRenderedImage();
    if (renderedImage == null) {
      return null;
    } else if (renderedImage instanceof BufferedImage) {
      return (BufferedImage)renderedImage;

    } else {
      final int width = getImageWidth();
      final int height = getImageHeight();
      final BufferedImage bufferedImage = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_ARGB);
      final Graphics2D g2 = bufferedImage.createGraphics();
      g2.drawRenderedImage(renderedImage, null);
      g2.dispose();
      return bufferedImage;
    }
  }

  @Override
  public CoordinateSystem getCoordinateSystem() {
    return this.geometryFactory.getCoordinateSystem();
  }

  @Override
  public int[] getDpi() {
    if (this.dpi == null) {
      int[] dpi = new int[] {
        96, 96
      };
      try {
        final Resource imageResource = getImageResource();
        final InputStream in = imageResource.getInputStream();
        final ImageInputStream iis = ImageIO.createImageInputStream(in);
        final Iterator<ImageReader> i = ImageIO.getImageReaders(iis);
        if (i.hasNext()) {
          final ImageReader r = i.next();
          r.setInput(iis);

          dpi = getResolution(r);

          if (dpi[0] == 0) {
            dpi[0] = 96;
          }
          if (dpi[1] == 0) {
            dpi[1] = 96;
          }

          r.dispose();
        }
        iis.close();
      } catch (final Throwable e) {
        e.printStackTrace();
      }
      this.dpi = dpi;
    }
    return this.dpi;
  }

  public File getFile() {
    return this.file;
  }

  @Override
  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  @Override
  public AbstractGeoReferencedImage getImage(
    final CoordinateSystem coordinateSystem) {
    synchronized (this.projectedImages) {
      if (coordinateSystem.equals(getCoordinateSystem())) {
        return this;
      } else {
        AbstractGeoReferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
        if (projectedImage == null) {
          projectedImage = getImage(coordinateSystem, this.resolution);
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
        return projectedImage;
      }
    }
  }

  @Override
  public AbstractGeoReferencedImage getImage(
    final CoordinateSystem coordinateSystem, final double resolution) {
    final int imageSrid = getGeometryFactory().getSrid();
    if (imageSrid > 0 && imageSrid != coordinateSystem.getId()) {
      final BoundingBox boundingBox = getBoundingBox();
      final ProjectionImageFilter filter = new ProjectionImageFilter(
        boundingBox, coordinateSystem, resolution);

      final BufferedImage newImage = filter.filter(getBufferedImage());

      final BoundingBox destBoundingBox = filter.getDestBoundingBox();
      return new BufferedGeoReferencedImage(destBoundingBox, newImage);
    }
    return this;
  }

  @Override
  public AbstractGeoReferencedImage getImage(
    final GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    return getImage(coordinateSystem);
  }

  @Override
  public double getImageAspectRatio() {
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    if (imageWidth > 0 && imageHeight > 0) {
      return (double)imageWidth / imageHeight;
    } else {
      return 0;
    }
  }

  @Override
  public int getImageHeight() {
    if (this.imageHeight == -1) {
      if (this.renderedImage != null) {
        this.imageHeight = this.renderedImage.getHeight();
      }
    }
    return this.imageHeight;
  }

  @Override
  public Resource getImageResource() {
    return this.imageResource;
  }

  @Override
  public int getImageWidth() {
    if (this.imageWidth == -1) {
      if (this.renderedImage != null) {
        this.imageWidth = this.renderedImage.getWidth();
      }
    }
    return this.imageWidth;
  }

  @Override
  public List<Dimension> getOverviewSizes() {
    return this.overviewSizes;
  }

  @Override
  public RenderedImage getRenderedImage() {
    return this.renderedImage;
  }

  @Override
  public double getResolution() {
    return this.resolution;
  }

  @Override
  public List<MappedLocation> getTiePoints() {
    return this.tiePoints;
  }

  @Override
  public String getWorldFileExtension() {
    return "tfw";
  }

  @Override
  public boolean hasBoundingBox() {
    return !this.boundingBox.isEmpty();
  }

  @Override
  public boolean hasGeometryFactory() {
    return this.geometryFactory.getSrid() > 0;
  }

  @Override
  public int hashCode() {
    return this.boundingBox.hashCode();
  }

  @Override
  public boolean isHasChanages() {
    return this.hasChanges;
  }

  protected void loadAuxXmlFile(final long modifiedTime) {
    final Resource resource = getImageResource();

    final String extension = SpringUtil.getFileNameExtension(resource);
    final Resource auxFile = SpringUtil.getResourceWithExtension(resource,
      extension + ".aux.xml");
    if (auxFile.exists() && SpringUtil.getLastModified(auxFile) > modifiedTime) {
      loadWorldFileX();
      final int[] dpi = getDpi();

      try {
        int srid = 0;
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        final DocumentBuilder builder = factory.newDocumentBuilder();
        final InputStream in = auxFile.getInputStream();
        try {
          final Document doc = builder.parse(in);
          final NodeList spatialReferences = doc.getElementsByTagName("SpatialReference");
          for (int i = 0; i < spatialReferences.getLength() && srid == 0; i++) {
            final Node spatialReference = spatialReferences.item(i);
            Element sridElement = DomUtil.getFirstChildElement(
              spatialReference, "LatestWKID");
            if (sridElement == null) {
              sridElement = DomUtil.getFirstChildElement(spatialReference,
                "WKID");
            }
            if (sridElement != null) {
              srid = DomUtil.getInteger(sridElement);
            }
          }
          if (srid == 0) {
            final NodeList srsList = doc.getElementsByTagName("SRS");
            for (int i = 0; i < srsList.getLength() && srid == 0; i++) {
              final Node srsNode = srsList.item(i);
              final String srsWkt = srsNode.getTextContent();
              final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(srsWkt);
              if (coordinateSystem != null) {
                srid = coordinateSystem.getId();
              }
            }
          }
          final GeometryFactory geometryFactory = GeometryFactory.floating(
            srid, 2);
          setGeometryFactory(geometryFactory);

          final List<Double> sourceControlPoints = DomUtil.getDoubleList(doc,
            "SourceGCPs");
          final List<Double> targetControlPoints = DomUtil.getDoubleList(doc,
            "TargetGCPs");
          if (sourceControlPoints.size() > 0 && targetControlPoints.size() > 0) {
            final List<MappedLocation> tiePoints = new ArrayList<MappedLocation>();
            for (int i = 0; i < sourceControlPoints.size()
              && i < targetControlPoints.size(); i += 2) {
              final double imageX = sourceControlPoints.get(i) * dpi[0];
              final double imageY = sourceControlPoints.get(i + 1) * dpi[1];
              final Point sourcePixel = new PointDouble(imageX, imageY);

              final double x = targetControlPoints.get(i);
              final double y = targetControlPoints.get(i + 1);
              final Point targetPoint = geometryFactory.point(x, y);
              final MappedLocation tiePoint = new MappedLocation(sourcePixel,
                targetPoint);
              tiePoints.add(tiePoint);
            }
            setTiePoints(tiePoints);
          }
        } finally {
          FileUtil.closeSilent(in);
        }

      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to read: " + auxFile,
          e);
      }

    }
  }

  protected void loadImageMetaData() {
    loadMetaDataFromImage();
    final long modifiedTime = loadSettings();
    loadAuxXmlFile(modifiedTime);
    if (!hasGeometryFactory()) {
      loadProjectionFile();
    }
    if (!hasBoundingBox()) {
      loadWorldFile();
    }
  }

  protected void loadMetaDataFromImage() {
  }

  protected void loadProjectionFile() {
    final Resource resource = getImageResource();
    final Resource projectionFile = SpringUtil.getResourceWithExtension(
      resource, "prj");
    if (projectionFile.exists()) {
      final CoordinateSystem coordinateSystem = EsriCoordinateSystems.getCoordinateSystem(projectionFile);
      setCoordinateSystem(coordinateSystem);
    }
  }

  protected long loadSettings() {
    final Resource resource = getImageResource();
    final Resource settingsFile = SpringUtil.addExtension(resource, "rgobject");
    if (settingsFile.exists()) {
      try {
        final Map<String, Object> settings = JsonMapIoFactory.toMap(settingsFile);
        final String boundingBoxWkt = (String)settings.get("boundingBox");
        if (StringUtils.hasText(boundingBoxWkt)) {
          final BoundingBox boundingBox = BoundingBoxDoubleGf.create(boundingBoxWkt);
          if (!boundingBox.isEmpty()) {
            setBoundingBox(boundingBox);
          }
        }

        final List<?> tiePointsProperty = (List<?>)settings.get("tiePoints");
        final List<MappedLocation> tiePoints = new ArrayList<MappedLocation>();
        if (tiePointsProperty != null) {
          for (final Object tiePointValue : tiePointsProperty) {
            if (tiePointValue instanceof MappedLocation) {
              tiePoints.add((MappedLocation)tiePointValue);
            } else if (tiePointValue instanceof Map) {
              @SuppressWarnings("unchecked")
              final Map<String, Object> map = (Map<String, Object>)tiePointValue;
              tiePoints.add(new MappedLocation(map));
            }
          }
        }
        if (!tiePoints.isEmpty()) {
          setTiePoints(tiePoints);
        }

        return SpringUtil.getLastModified(settingsFile);
      } catch (final Throwable e) {
        ExceptionUtil.log(getClass(), "Unable to load:" + settingsFile, e);
        return -1;
      }
    } else {
      return -1;
    }
  }

  protected void loadWorldFile() {
    final Resource resource = getImageResource();
    final Resource worldFile = SpringUtil.getResourceWithExtension(resource,
      getWorldFileExtension());
    loadWorldFile(worldFile);
  }

  @SuppressWarnings("unused")
  protected void loadWorldFile(final Resource worldFile) {
    // if (boundingBox.isEmpty()) {
    if (worldFile.exists()) {
      try {
        final BufferedReader reader = SpringUtil.getBufferedReader(worldFile);
        try {
          final double pixelWidth = Double.parseDouble(reader.readLine());
          final double yRotation = Double.parseDouble(reader.readLine());
          final double xRotation = Double.parseDouble(reader.readLine());
          final double pixelHeight = Double.parseDouble(reader.readLine());
          // Top left
          final double x1 = Double.parseDouble(reader.readLine());
          final double y1 = Double.parseDouble(reader.readLine());
          setResolution(pixelWidth);
          // TODO rotation using a warp filter
          setBoundingBox(x1, y1, pixelWidth, -pixelHeight);
          // worldWarpFilter = new WarpAffineFilter(new BoundingBoxDoubleGf(
          // getGeometryFactory(), 0, 0, imageWidth, imageHeight), imageWidth,
          // imageHeight, x1, y1, pixelWidth, -pixelHeight, xRotation,
          // yRotation);
        } finally {
          reader.close();
        }
      } catch (final IOException e) {
        LoggerFactory.getLogger(getClass()).error(
          "Error reading world file " + worldFile, e);
      }
    }
    // }
  }

  protected void loadWorldFileX() {
    final Resource resource = getImageResource();
    final Resource worldFile = SpringUtil.getResourceWithExtension(resource,
      getWorldFileExtension() + "x");
    if (worldFile.exists()) {
      loadWorldFile(worldFile);
    } else {
      loadWorldFile();
    }

  }

  protected void postConstruct() {
    setHasChanges(false);
    Property.addListener(this.tiePoints, this);
  }

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == this.tiePoints) {
      if (event instanceof IndexedPropertyChangeEvent) {
        final Object oldValue = event.getOldValue();
        if (oldValue instanceof MappedLocation) {
          ((MappedLocation)oldValue).removeListener(this);
        }
        final Object newValue = event.getOldValue();
        if (newValue instanceof MappedLocation) {
          ((MappedLocation)newValue).addListener(this);
        }
      }
    } else if (source instanceof MappedLocation) {
      setHasChanges(true);
    }
    firePropertyChange(event);
  }

  @Override
  public boolean saveChanges() {
    try {
      final Resource rgResource = SpringUtil.addExtension(this.imageResource,
        "rgobject");
      MapObjectFactoryRegistry.write(rgResource, this);
      setHasChanges(false);
      return true;
    } catch (final Throwable e) {
      ExceptionUtil.log(getClass(), "Unable to save: " + this.imageResource
        + ".rgobject", e);
      return false;
    }
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    if (!EqualsRegistry.equal(boundingBox, this.boundingBox)) {
      setGeometryFactory(boundingBox.getGeometryFactory());
      this.boundingBox = boundingBox;
      setHasChanges(true);
    }
  }

  @Override
  public void setBoundingBox(final double x1, final double y1,
    final double pixelWidth, final double pixelHeight) {
    final GeometryFactory geometryFactory = getGeometryFactory();

    final int imageWidth = getImageWidth();
    final double x2 = x1 + pixelWidth * imageWidth;

    final int imageHeight = getImageHeight();
    final double y2 = y1 - pixelHeight * imageHeight;
    final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2,
      x1, y1, x2, y2);
    setBoundingBox(boundingBox);
  }

  @Override
  public void setCoordinateSystem(final CoordinateSystem coordinateSystem) {
    setGeometryFactory(coordinateSystem.getGeometryFactory());
  }

  @Override
  public void setDpi(final int... dpi) {
    this.dpi = dpi;
  }

  @Override
  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory.convertAxisCount(2);
    for (final MappedLocation mappedLocation : this.tiePoints) {
      mappedLocation.setGeometryFactory(geometryFactory);
    }
  }

  protected void setHasChanges(final boolean hasChanges) {
    this.hasChanges = hasChanges;
  }

  protected void setImageHeight(final int imageHeight) {
    this.imageHeight = imageHeight;
  }

  protected void setImageResource(final Resource imageResource) {
    this.imageResource = imageResource;
    this.file = SpringUtil.getOrDownloadFile(this.imageResource);
  }

  protected void setImageWidth(final int imageWidth) {
    this.imageWidth = imageWidth;
  }

  protected void setOverviewSizes(final List<Dimension> overviewSizes) {
    this.overviewSizes = overviewSizes;
  }

  @Override
  public void setRenderedImage(final RenderedImage renderedImage) {
    this.renderedImage = renderedImage;
  }

  protected void setResolution(final double resolution) {
    this.resolution = resolution;
  }

  @Override
  public void setTiePoints(final List<MappedLocation> tiePoints) {
    if (!EqualsRegistry.equal(tiePoints, this.tiePoints)) {
      for (final MappedLocation mappedLocation : this.tiePoints) {
        mappedLocation.removeListener(this);
      }
      this.tiePoints.clear();
      this.tiePoints.addAll(tiePoints);
      final GeometryFactory geometryFactory = getGeometryFactory();
      for (final MappedLocation mappedLocation : tiePoints) {
        mappedLocation.setGeometryFactory(geometryFactory);
        mappedLocation.addListener(this);
      }
      setHasChanges(true);
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "bufferedImage");
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox != null) {
      MapSerializerUtil.add(map, "boundingBox", boundingBox.toString());
    }
    final List<MappedLocation> tiePoints = getTiePoints();
    MapSerializerUtil.add(map, "tiePoints", tiePoints);
    return map;
  }

  @Override
  public String toString() {
    if (this.imageResource == null) {
      return super.toString();
    } else {
      return this.imageResource.toString();
    }
  }
}
