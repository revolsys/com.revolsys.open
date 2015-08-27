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
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.revolsys.beans.AbstractPropertyChangeObject;
import com.revolsys.collection.PropertyChangeArrayList;
import com.revolsys.data.equals.Equals;
import com.revolsys.format.json.Json;
import com.revolsys.format.xml.DomUtil;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.BoundingBoxDoubleGf;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.math.matrix.Matrix;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public abstract class AbstractGeoreferencedImage extends AbstractPropertyChangeObject
  implements GeoreferencedImage {

  private static double[] calculateLSM(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final List<MappedLocation> mappings) {

    final Matrix A = getAMatrix(mappings, imageHeight);

    final Matrix X = getXMatrix(boundingBox, imageWidth, imageHeight, mappings);

    final Matrix P = getWeights(mappings.size());

    final Matrix AT = A.transpose();

    final Matrix ATP = new Matrix(AT.getRowCount(), P.getColumnCount());
    final Matrix ATPA = new Matrix(AT.getRowCount(), A.getColumnCount());
    final Matrix ATPX = new Matrix(AT.getRowCount(), 1);
    final Matrix x = new Matrix(A.getColumnCount(), 1);
    ATP.times(AT, P);
    ATPA.times(ATP, A);
    ATPX.times(ATP, X);
    ATPA.invert();
    x.times(ATPA, ATPX);
    ATPA.invert();

    return x.transpose().getRow(0);
  }

  public static Matrix getAMatrix(final List<MappedLocation> mappings, final int imageHeight) {
    final int mappingCount = mappings.size();
    final int rowCount = mappingCount * 2;
    final Matrix aMatrix = new Matrix(rowCount, 6);

    for (int j = 0; j < mappingCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Point sourcePoint = mappedLocation.getSourcePixel();
      final double x = sourcePoint.getX();
      final double y = imageHeight - sourcePoint.getY();
      aMatrix.setRow(j, x, y, 1.0D, 0.0D, 0.0D, 0.0D);
    }

    for (int j = mappingCount; j < rowCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - mappingCount);
      final Point sourcePoint = mappedLocation.getSourcePixel();
      final double x = sourcePoint.getX();
      final double y = imageHeight - sourcePoint.getY();
      aMatrix.setRow(j, 0.0D, 0.0D, 0.0D, x, y, 1.0D);
    }
    return aMatrix;
  }

  public static int[] getResolution(final ImageReader r) throws IOException {
    int hdpi = 96, vdpi = 96;
    final double mm2inch = 25.4;

    NodeList lst;
    final Element node = (Element)r.getImageMetadata(0).getAsTree("javax_imageio_1.0");
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

  public static Matrix getWeights(final int size) {
    final int matrixSize = size * 2;
    final Matrix P = new Matrix(matrixSize, matrixSize);

    for (int j = 0; j < matrixSize; ++j) {
      P.set(j, j, 1.0D);
    }
    return P;
  }

  private static Matrix getXMatrix(final BoundingBox boundingBox, final int imageWidth,
    final int imageHeight, final List<MappedLocation> mappings) {
    final int mappingCount = mappings.size();
    final int rowCount = mappingCount * 2;
    final Matrix xMatrix = new Matrix(rowCount, 1);

    for (int j = 0; j < mappingCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j);
      final Point targetPixel = mappedLocation.getTargetPixel(boundingBox, imageWidth, imageHeight);
      final double x = targetPixel.getX();
      xMatrix.set(j, 0, x);
    }

    for (int j = mappingCount; j < rowCount; ++j) {
      final MappedLocation mappedLocation = mappings.get(j - mappingCount);
      final Point targetPixel = mappedLocation.getTargetPixel(boundingBox, imageWidth, imageHeight);
      final double y = imageHeight - targetPixel.getY();
      xMatrix.set(j, 0, y);
    }
    return xMatrix;
  }

  private BoundingBox boundingBox = BoundingBox.EMPTY;

  private int[] dpi;

  private File file;

  private GeometryFactory geometryFactory = GeometryFactory.floating3();

  private boolean hasChanges;

  private int imageHeight = -1;

  private Resource imageResource;

  private int imageWidth = -1;

  private List<Dimension> overviewSizes = new ArrayList<>();

  private final Map<CoordinateSystem, AbstractGeoreferencedImage> projectedImages = new HashMap<CoordinateSystem, AbstractGeoreferencedImage>();

  private RenderedImage renderedImage;

  private double resolution;

  private final PropertyChangeArrayList<MappedLocation> tiePoints = new PropertyChangeArrayList<MappedLocation>();

  public AbstractGeoreferencedImage() {
  }

  protected void addOverviewSize(final int width, final int height) {
    final Dimension size = new Dimension(width, height);
    this.overviewSizes.add(size);
  }

  @Override
  public void cancelChanges() {
  }

  @Override
  public void deleteTiePoint(final MappedLocation tiePoint) {
    if (this.tiePoints.remove(tiePoint)) {
      this.hasChanges = true;
    }
  }

  @Override
  public void drawImage(final Graphics2D graphics, final BoundingBox viewBoundingBox,
    final int viewWidth, final int viewHeight, final boolean useTransform) {
    final BoundingBox imageBoundingBox = getBoundingBox();
    if (viewBoundingBox.intersects(imageBoundingBox) && viewWidth > 0 && viewHeight > 0) {
      final RenderedImage renderedImage = getRenderedImage();
      drawRenderedImage(renderedImage, graphics, viewBoundingBox, viewWidth, viewHeight,
        useTransform);
    }
  }

  public void drawRenderedImage(final RenderedImage renderedImage, BoundingBox imageBoundingBox,
    final Graphics2D graphics, final BoundingBox viewBoundingBox, final int viewWidth,
    final boolean useTransform) {
    if (renderedImage != null) {
      final int imageWidth = renderedImage.getWidth();
      final int imageHeight = renderedImage.getHeight();
      if (imageWidth > 0 && imageHeight > 0) {

        imageBoundingBox = imageBoundingBox.convert(viewBoundingBox.getGeometryFactory());
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
          final int imageScreenWidth = (int)Math.ceil(imageModelWidth * scaleFactor);

          final double imageModelHeight = imageBoundingBox.getHeight();
          final int imageScreenHeight = (int)Math.ceil(imageModelHeight * scaleFactor);

          if (imageScreenWidth > 0 && imageScreenWidth < 10000 && imageScreenHeight > 0
            && imageScreenHeight < 10000) {
            graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
              RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            if (imageScreenWidth > 0 && imageScreenHeight > 0) {

              graphics.translate(screenX, screenY);
              if (renderedImage instanceof BufferedImage && !useTransform) {
                final BufferedImage bufferedImage = (BufferedImage)renderedImage;
                try {
                  graphics.drawImage(bufferedImage, 0, 0, imageScreenWidth, imageScreenHeight,
                    null);
                } catch (final Throwable e) {
                  LoggerFactory.getLogger(getClass())
                    .error(imageScreenWidth + "x" + imageScreenHeight, e);
                }
              } else {
                final double scaleX = (double)imageScreenWidth / imageWidth;
                final double scaleY = (double)imageScreenHeight / imageHeight;
                final AffineTransform imageTransform = new AffineTransform(scaleX, 0, 0, scaleY, 0,
                  0);
                if (useTransform) {
                  final AffineTransform geoTransform = getAffineTransformation(imageBoundingBox);
                  imageTransform.concatenate(geoTransform);
                }
                graphics.drawRenderedImage(renderedImage, imageTransform);
              }
            }
          }
        } catch (final Throwable e) {
        } finally {
          graphics.setTransform(transform);
        }
      }
    }
  }

  protected void drawRenderedImage(final RenderedImage renderedImage, final Graphics2D graphics,
    final BoundingBox viewBoundingBox, final int viewWidth, final int viewHeight,
    final boolean useTransform) {
    final BoundingBox imageBoundingBox = getBoundingBox();
    drawRenderedImage(renderedImage, imageBoundingBox, graphics, viewBoundingBox, viewWidth,
      useTransform);
  }

  @Override
  public AffineTransform getAffineTransformation(final BoundingBox boundingBox) {
    final List<MappedLocation> mappings = new ArrayList<>(getTiePoints());
    final int count = mappings.size();
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    if (count == 1) {
      final MappedLocation tiePoint = mappings.get(0);
      final Point sourcePixel = tiePoint.getSourcePixel();
      final Point targetPixel = tiePoint.getTargetPixel(boundingBox, imageWidth, imageHeight);
      final double translateX = targetPixel.getX() - sourcePixel.getX();
      final double translateY = sourcePixel.getY() - targetPixel.getY();
      return new AffineTransform(1, 0, 0, 1, translateX, translateY);
    } else if (count < 3) {
      return new AffineTransform();
    }
    final double[] affineTransformMatrix = calculateLSM(boundingBox, imageWidth, imageHeight,
      mappings);
    final double translateX = affineTransformMatrix[2];
    final double translateY = affineTransformMatrix[5];
    final double scaleX = affineTransformMatrix[0];
    final double scaleY = affineTransformMatrix[4];
    final double shearX = affineTransformMatrix[1];
    final double shearY = affineTransformMatrix[3];
    return new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
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
  public AbstractGeoreferencedImage getImage(final CoordinateSystem coordinateSystem) {
    synchronized (this.projectedImages) {
      if (coordinateSystem.equals(getCoordinateSystem())) {
        return this;
      } else {
        AbstractGeoreferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
        if (projectedImage == null) {
          projectedImage = getImage(coordinateSystem, this.resolution);
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
        return projectedImage;
      }
    }
  }

  @Override
  public AbstractGeoreferencedImage getImage(final CoordinateSystem coordinateSystem,
    final double resolution) {
    final int imageSrid = getGeometryFactory().getSrid();
    if (imageSrid > 0 && imageSrid != coordinateSystem.getId()) {
      final BoundingBox boundingBox = getBoundingBox();
      final ProjectionImageFilter filter = new ProjectionImageFilter(boundingBox, coordinateSystem,
        resolution);

      final BufferedImage newImage = filter.filter(getBufferedImage());

      final BoundingBox destBoundingBox = filter.getDestBoundingBox();
      return new BufferedGeoreferencedImage(destBoundingBox, newImage);
    }
    return this;
  }

  @Override
  public AbstractGeoreferencedImage getImage(final GeometryFactory geometryFactory) {
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

  @Override
  public boolean isHasTransform() {
    final int count = getTiePoints().size();
    if (count > 2 || count == 1) {
      return true;
    } else {
      return false;
    }
  }

  protected void loadAuxXmlFile(final long modifiedTime) {
    final Resource resource = getImageResource();

    final String extension = resource.getFileNameExtension();
    final Resource auxFile = resource.createChangeExtension(extension + ".aux.xml");
    if (auxFile.exists() && auxFile.getLastModified() > modifiedTime) {
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
            Element sridElement = DomUtil.getFirstChildElement(spatialReference, "LatestWKID");
            if (sridElement == null) {
              sridElement = DomUtil.getFirstChildElement(spatialReference, "WKID");
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
              final CoordinateSystem coordinateSystem = EsriCoordinateSystems
                .getCoordinateSystem(srsWkt);
              if (coordinateSystem != null) {
                srid = coordinateSystem.getId();
              }
            }
          }
          final GeometryFactory geometryFactory = GeometryFactory.floating(srid, 2);
          setGeometryFactory(geometryFactory);

          final List<Double> sourceControlPoints = DomUtil.getDoubleList(doc, "SourceGCPs");
          final List<Double> targetControlPoints = DomUtil.getDoubleList(doc, "TargetGCPs");
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
              final MappedLocation tiePoint = new MappedLocation(sourcePixel, targetPoint);
              tiePoints.add(tiePoint);
            }
            setTiePoints(tiePoints);
          }
        } finally {
          FileUtil.closeSilent(in);
        }

      } catch (final Throwable e) {
        LoggerFactory.getLogger(getClass()).error("Unable to read: " + auxFile, e);
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
    final GeometryFactory geometryFactory = EsriCoordinateSystems.getGeometryFactory(resource);
    setGeometryFactory(geometryFactory);
  }

  protected long loadSettings() {
    final Resource resource = getImageResource();
    final Resource settingsFile = resource.createAddExtension("rgobject");
    if (settingsFile.exists()) {
      try {
        final Map<String, Object> settings = Json.toMap(settingsFile);
        final String boundingBoxWkt = (String)settings.get("boundingBox");
        if (Property.hasValue(boundingBoxWkt)) {
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

        return settingsFile.getLastModified();
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
    final Resource worldFile = resource.createChangeExtension(getWorldFileExtension());
    loadWorldFile(worldFile);
  }

  @SuppressWarnings("unused")
  protected void loadWorldFile(final Resource worldFile) {
    if (worldFile.exists()) {
      try {
        try (
          final BufferedReader reader = worldFile.newBufferedReader()) {
          final double pixelWidth = Double.parseDouble(reader.readLine());
          final double yRotation = Double.parseDouble(reader.readLine());
          final double xRotation = Double.parseDouble(reader.readLine());
          final double pixelHeight = Double.parseDouble(reader.readLine());
          // Top left
          final double x1 = Double.parseDouble(reader.readLine());
          final double y1 = Double.parseDouble(reader.readLine());
          setResolution(pixelWidth);
          // TODO rotation using a warp filter
          setBoundingBox(x1, y1, pixelWidth, pixelHeight);
          // worldWarpFilter = new WarpAffineFilter(new BoundingBoxDoubleGf(
          // getGeometryFactory(), 0, 0, imageWidth, imageHeight), imageWidth,
          // imageHeight, x1, y1, pixelWidth, -pixelHeight, xRotation,
          // yRotation);
        }
      } catch (final IOException e) {
        LoggerFactory.getLogger(getClass()).error("Error reading world file " + worldFile, e);
      }
    }
  }

  protected void loadWorldFileX() {
    final Resource resource = getImageResource();
    final Resource worldFile = resource.createChangeExtension(getWorldFileExtension() + "x");
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
      setHasChanges(true);
    } else if (source instanceof MappedLocation) {
      setHasChanges(true);
    }
    firePropertyChange(event);
  }

  @Override
  public boolean saveChanges() {
    try {
      final Resource resource = this.imageResource;
      final Resource rgResource = resource.createAddExtension("rgobject");
      MapObjectFactoryRegistry.write(rgResource, this);
      setHasChanges(false);
      return true;
    } catch (final Throwable e) {
      ExceptionUtil.log(getClass(), "Unable to save: " + this.imageResource + ".rgobject", e);
      return false;
    }
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    if (!Equals.equal(boundingBox, this.boundingBox)) {
      setGeometryFactory(boundingBox.getGeometryFactory());
      this.boundingBox = boundingBox;
      setHasChanges(true);
    }
  }

  @Override
  public void setBoundingBox(final double minX, final double maxY, final double pixelWidth,
    final double pixelHeight) {
    final GeometryFactory geometryFactory = getGeometryFactory();

    final int imageWidth = getImageWidth();
    final double maxX = minX + pixelWidth * imageWidth;

    final int imageHeight = getImageHeight();
    final double minY = maxY + pixelHeight * imageHeight;
    final BoundingBox boundingBox = new BoundingBoxDoubleGf(geometryFactory, 2, minX, maxY, maxX,
      minY);
    setBoundingBox(boundingBox);
  }

  @Override
  public void setDpi(final int... dpi) {
    this.dpi = dpi;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    if (geometryFactory != null) {
      this.geometryFactory = geometryFactory.convertAxisCount(2);
      for (final MappedLocation mappedLocation : this.tiePoints) {
        mappedLocation.setGeometryFactory(geometryFactory);
      }
    }
  }

  protected void setHasChanges(final boolean hasChanges) {
    final boolean oldValue = this.hasChanges;
    this.hasChanges = hasChanges;
    firePropertyChange("hasChanges", oldValue, hasChanges);
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
    if (!Equals.equal(tiePoints, this.tiePoints)) {
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
