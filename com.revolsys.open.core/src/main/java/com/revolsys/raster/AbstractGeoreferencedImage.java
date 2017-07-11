package com.revolsys.raster;

import java.awt.Dimension;
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
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.revolsys.beans.AbstractPropertyChangeSupportProxy;
import com.revolsys.collection.PropertyChangeArrayList;
import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.datatype.DataType;
import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.esri.EsriCoordinateSystems;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.geometry.model.impl.PointDouble;
import com.revolsys.io.FileUtil;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.logging.Logs;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.xml.DomUtil;
import com.revolsys.spring.resource.Resource;
import com.revolsys.spring.resource.SpringUtil;
import com.revolsys.util.Property;

public abstract class AbstractGeoreferencedImage extends AbstractPropertyChangeSupportProxy
  implements GeoreferencedImage {

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

  private BoundingBox boundingBox = BoundingBox.EMPTY;

  private int[] dpi;

  private File file;

  private GeometryFactory geometryFactory = GeometryFactory.DEFAULT_3D;

  private boolean hasChanges;

  private int imageHeight = -1;

  private Resource imageResource;

  private int imageWidth = -1;

  private List<Dimension> overviewSizes = new ArrayList<>();

  private final Map<CoordinateSystem, AbstractGeoreferencedImage> projectedImages = new HashMap<>();

  private RenderedImage renderedImage;

  private double resolution;

  private final PropertyChangeArrayList<MappedLocation> tiePoints = new PropertyChangeArrayList<>();

  public AbstractGeoreferencedImage() {
  }

  protected void addOverviewSize(final int width, final int height) {
    final Dimension size = new Dimension(width, height);
    this.overviewSizes.add(size);
  }

  @Override
  public void deleteTiePoint(final MappedLocation tiePoint) {
    if (this.tiePoints.remove(tiePoint)) {
      this.hasChanges = true;
    }
  }

  @Override
  public BoundingBox getBoundingBox() {
    return this.boundingBox;
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
    final int imageSrid = getGeometryFactory().getCoordinateSystemId();
    if (imageSrid > 0 && imageSrid != coordinateSystem.getCoordinateSystemId()) {
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
  public int hashCode() {
    return this.boundingBox.hashCode();
  }

  @Override
  public boolean isHasChanages() {
    return this.hasChanges;
  }

  protected void loadAuxXmlFile(final long modifiedTime) {
    final Resource resource = getImageResource();

    final String extension = resource.getFileNameExtension();
    final Resource auxFile = resource.newResourceChangeExtension(extension + ".aux.xml");
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
                srid = coordinateSystem.getCoordinateSystemId();
              }
            }
          }
          final GeometryFactory geometryFactory = GeometryFactory.floating(srid, 2);
          setGeometryFactory(geometryFactory);

          final List<Double> sourceControlPoints = DomUtil.getDoubleList(doc, "SourceGCPs");
          final List<Double> targetControlPoints = DomUtil.getDoubleList(doc, "TargetGCPs");
          if (sourceControlPoints.size() > 0 && targetControlPoints.size() > 0) {
            final List<MappedLocation> tiePoints = new ArrayList<>();
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
        Logs.error(this, "Unable to read: " + auxFile, e);
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
    final Resource settingsFile = resource.newResourceAddExtension("rgobject");
    if (settingsFile.exists()) {
      try {
        final Map<String, Object> settings = Json.toMap(settingsFile);
        final String boundingBoxWkt = (String)settings.get("boundingBox");
        if (Property.hasValue(boundingBoxWkt)) {
          final BoundingBox boundingBox = BoundingBox.newBoundingBox(boundingBoxWkt);
          if (!boundingBox.isEmpty()) {
            setBoundingBox(boundingBox);
          }
        }

        final List<?> tiePointsProperty = (List<?>)settings.get("tiePoints");
        final List<MappedLocation> tiePoints = new ArrayList<>();
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
        Logs.error(this, "Unable to load:" + settingsFile, e);
        return -1;
      }
    } else {
      return -1;
    }
  }

  protected void loadWorldFile() {
    final Resource resource = getImageResource();
    final Resource worldFile = resource.newResourceChangeExtension(getWorldFileExtension());
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
        Logs.error(this, "Error reading world file " + worldFile, e);
      }
    }
  }

  protected void loadWorldFileX() {
    final Resource resource = getImageResource();
    final Resource worldFile = resource.newResourceChangeExtension(getWorldFileExtension() + "x");
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
          ((MappedLocation)oldValue).removePropertyChangeListener(this);
        }
        final Object newValue = event.getOldValue();
        if (newValue instanceof MappedLocation) {
          ((MappedLocation)newValue).addPropertyChangeListener(this);
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
      final Resource rgResource = resource.newResourceAddExtension("rgobject");
      MapObjectFactory.write(rgResource, this);
      setHasChanges(false);
      return true;
    } catch (final Throwable e) {
      Logs.error(this, "Unable to save: " + this.imageResource + ".rgobject", e);
      return false;
    }
  }

  @Override
  public void setBoundingBox(final BoundingBox boundingBox) {
    if (!DataType.equal(boundingBox, this.boundingBox)) {
      setGeometryFactory(boundingBox.getGeometryFactory());
      this.boundingBox = boundingBox;
      setHasChanges(true);
    }
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
    if (!DataType.equal(tiePoints, this.tiePoints)) {
      for (final MappedLocation mappedLocation : this.tiePoints) {
        mappedLocation.removePropertyChangeListener(this);
      }
      this.tiePoints.clear();
      this.tiePoints.addAll(tiePoints);
      final GeometryFactory geometryFactory = getGeometryFactory();
      for (final MappedLocation mappedLocation : tiePoints) {
        mappedLocation.setGeometryFactory(geometryFactory);
        mappedLocation.addPropertyChangeListener(this);
      }
      setHasChanges(true);
    }
  }

  @Override
  public MapEx toMap() {
    final MapEx map = new LinkedHashMapEx();
    addTypeToMap(map, "bufferedImage");
    final BoundingBox boundingBox = getBoundingBox();
    if (boundingBox != null) {
      addToMap(map, "boundingBox", boundingBox.toString());
    }
    final List<MappedLocation> tiePoints = getTiePoints();
    addToMap(map, "tiePoints", tiePoints);
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
