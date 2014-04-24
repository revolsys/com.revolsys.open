package com.revolsys.swing.map.layer.raster;

import java.awt.image.BufferedImage;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
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
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RenderedOp;
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
import com.revolsys.gis.cs.CoordinateSystem;
import com.revolsys.gis.cs.esri.EsriCoordinateSystems;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.gis.model.data.equals.EqualsRegistry;
import com.revolsys.io.FileUtil;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.io.map.MapSerializerUtil;
import com.revolsys.io.xml.DomUtil;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Coordinates;
import com.revolsys.jts.geom.Envelope;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.jts.geom.Point;
import com.revolsys.spring.SpringUtil;
import com.revolsys.swing.map.layer.MapTile;
import com.revolsys.swing.map.layer.raster.filter.WarpAffineFilter;
import com.revolsys.swing.map.layer.raster.filter.WarpFilter;
import com.revolsys.swing.map.overlay.MappedLocation;
import com.revolsys.util.ExceptionUtil;
import com.revolsys.util.Property;

public class GeoReferencedImage extends AbstractPropertyChangeObject implements
  PropertyChangeListener, MapSerializer {

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

  private BoundingBox boundingBox = new Envelope();

  private BufferedImage image;

  private int imageWidth = -1;

  private int imageHeight = -1;

  private GeometryFactory geometryFactory = GeometryFactory.getFactory();

  private PlanarImage jaiImage;

  private Resource imageResource;

  private double resolution;

  private final Map<CoordinateSystem, GeoReferencedImage> projectedImages = new HashMap<CoordinateSystem, GeoReferencedImage>();

  private final PropertyChangeArrayList<MappedLocation> tiePoints = new PropertyChangeArrayList<MappedLocation>();

  private WarpFilter warpFilter;

  private BufferedImage warpedImage;

  private final int degree = 1;

  private boolean hasChanges;

  private int[] dpi;

  public GeoReferencedImage(final BoundingBox boundingBox,
    final BufferedImage image) {
    this(boundingBox, image.getWidth(), image.getHeight());
    setImage(image);
  }

  public GeoReferencedImage(final BoundingBox boundingBox,
    final int imageWidth, final int imageHeight) {
    this.boundingBox = boundingBox;
    this.geometryFactory = boundingBox.getGeometryFactory();
    this.imageWidth = imageWidth;
    this.imageHeight = imageHeight;
    Property.addListener(tiePoints, this);
  }

  public GeoReferencedImage(final Resource imageResource) {
    this.imageResource = imageResource;
    setImage(createBufferedImage());
    loadImageMetaData();
    setHasChanges(false);
    Property.addListener(tiePoints, this);
  }

  private void addMapping(final List<MappedLocation> mappings,
    final double imageX, final double imageY, final double modelX,
    final double modelY) {
    final DoubleCoordinates imagePoint = new DoubleCoordinates(imageX, imageY);
    final com.revolsys.jts.geom.GeometryFactory geometryFactory = getGeometryFactory();
    final Point modelPoint = geometryFactory.point(modelX, modelY);
    final MappedLocation mappedLocation = new MappedLocation(imagePoint,
      modelPoint);
    mappings.add(mappedLocation);
  }

  public void cancelChanges() {
    if (this.imageResource != null) {
      loadImageMetaData();
      setHasChanges(false);
    }
  }

  protected synchronized void clearWarp() {
    this.warpedImage = null;
    this.warpFilter = null;
  }

  protected BufferedImage createBufferedImage() {
    final File file = SpringUtil.getOrDownloadFile(this.imageResource);

    this.jaiImage = JAI.create("fileload", file.getAbsolutePath());
    return this.jaiImage.getAsBufferedImage();
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj instanceof MapTile) {
      final MapTile tile = (MapTile)obj;
      return tile.getBoundingBox().equals(this.boundingBox);
    }
    return false;
  }

  public BoundingBox getBoundingBox() {
    return this.boundingBox;
  }

  public CoordinateSystem getCoordinateSystem() {
    return this.geometryFactory.getCoordinateSystem();
  }

  protected int[] getDpi() {
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

  public GeometryFactory getGeometryFactory() {
    return this.geometryFactory;
  }

  public BufferedImage getImage() {
    if (getWarpFilter() == null) {
      return this.image;
    } else {
      return getWarpedImage();
    }
  }

  public GeoReferencedImage getImage(
    final com.revolsys.jts.geom.GeometryFactory geometryFactory) {
    final CoordinateSystem coordinateSystem = geometryFactory.getCoordinateSystem();
    return getImage(coordinateSystem);
  }

  public GeoReferencedImage getImage(final CoordinateSystem coordinateSystem) {
    synchronized (this.projectedImages) {
      if (coordinateSystem.equals(getCoordinateSystem())) {
        return this;
      } else {
        GeoReferencedImage projectedImage = this.projectedImages.get(coordinateSystem);
        if (projectedImage == null) {
          projectedImage = getImage(coordinateSystem, this.resolution);
          this.projectedImages.put(coordinateSystem, projectedImage);
        }
        return projectedImage;
      }
    }
  }

  public GeoReferencedImage getImage(final CoordinateSystem coordinateSystem,
    final double resolution) {
    final int imageSrid = getGeometryFactory().getSrid();
    if (imageSrid > 0 && imageSrid != coordinateSystem.getId()) {
      final BoundingBox boundingBox = getBoundingBox();
      final ProjectionImageFilter filter = new ProjectionImageFilter(
        boundingBox, coordinateSystem, resolution);

      final BufferedImage newImage = filter.filter(getImage());

      final BoundingBox destBoundingBox = filter.getDestBoundingBox();
      return new GeoReferencedImage(destBoundingBox, newImage);
    }
    return this;
  }

  public double getImageAspectRatio() {
    final int imageWidth = getImageWidth();
    final int imageHeight = getImageHeight();
    if (imageWidth > 0 && imageHeight > 0) {
      return (double)imageWidth / imageHeight;
    } else {
      return 0;
    }
  }

  public int getImageHeight() {
    if (this.imageHeight == -1) {
      this.imageHeight = this.image.getHeight();
    }
    return this.imageHeight;
  }

  public Resource getImageResource() {
    return this.imageResource;
  }

  public int getImageWidth() {
    if (this.imageWidth == -1) {
      this.imageWidth = this.image.getWidth();
    }
    return this.imageWidth;
  }

  public PlanarImage getJaiImage() {
    if (this.jaiImage == null && this.image != null) {
      this.jaiImage = PlanarImage.wrapRenderedImage(this.image);
    }
    return this.jaiImage;
  }

  public BufferedImage getOriginalImage() {
    return image;
  }

  public WarpFilter getOriginalWarpFilter() {
    return new WarpAffineFilter(getBoundingBox(), getImageWidth(),
      getImageHeight());
  }

  public double getResolution() {
    return resolution;
  }

  public List<MappedLocation> getTiePoints() {
    return tiePoints;
  }

  public synchronized BufferedImage getWarpedImage() {
    if (image == null || boundingBox.isEmpty()) {
      return this.image;
    } else if (this.warpedImage == null) {
      final WarpFilter warpFilter = getWarpFilter();
      this.warpedImage = warpFilter.filter(image);
    }
    return warpedImage;
  }

  public synchronized WarpFilter getWarpFilter() {
    if (this.image != null && this.warpFilter == null) {
      final int imageWidth = image.getWidth();
      final int imageHeight = image.getHeight();
      if (this.boundingBox.isEmpty()) {
        this.warpFilter = new WarpAffineFilter(boundingBox, imageWidth,
          imageHeight);
      } else {
        List<MappedLocation> mappings = getTiePoints();
        if (mappings.isEmpty()) {
          mappings = new ArrayList<MappedLocation>();
          final double minX = boundingBox.getMinX();
          final double maxX = boundingBox.getMaxX();
          final double minY = boundingBox.getMinY();
          final double maxY = boundingBox.getMaxY();
          addMapping(mappings, 0, 0, minX, minY);
          addMapping(mappings, imageWidth, 0, maxX, minY);
          addMapping(mappings, imageWidth, imageHeight, maxX, maxY);
          addMapping(mappings, 0, imageHeight, minX, maxY);
        }
        this.warpFilter = WarpFilter.createWarpFilter(boundingBox, mappings,
          this.degree, imageWidth, imageHeight);
      }
    }

    return warpFilter;
  }

  public String getWorldFileExtension() {
    return "tfw";
  }

  public boolean hasBoundingBox() {
    return !boundingBox.isEmpty();
  }

  public boolean hasGeometryFactory() {
    return geometryFactory.getSrid() > 0;
  }

  @Override
  public int hashCode() {
    return this.boundingBox.hashCode();
  }

  public boolean isHasChanages() {
    return hasChanges;
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
          final GeometryFactory geometryFactory = GeometryFactory.getFactory(
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
              final Coordinates sourcePixel = new DoubleCoordinates(imageX,
                imageY);

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

  private void loadImageMetaData() {
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
          final BoundingBox boundingBox = Envelope.create(boundingBoxWkt);
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
          // worldWarpFilter = new WarpAffineFilter(new Envelope(
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

  @Override
  public void propertyChange(final PropertyChangeEvent event) {
    final Object source = event.getSource();
    if (source == tiePoints) {
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
      clearWarp();
    } else if (source instanceof MappedLocation) {
      setHasChanges(true);
      clearWarp();
    }
    firePropertyChange(event);
  }

  public boolean saveChanges() {
    try {
      final Resource rgResource = SpringUtil.addExtension(imageResource,
        "rgobject");
      MapObjectFactoryRegistry.write(rgResource, this);
      setHasChanges(false);
      return true;
    } catch (final Throwable e) {
      ExceptionUtil.log(getClass(), "Unable to save: " + imageResource
        + ".rgobject", e);
      return false;
    }
  }

  public void setBoundingBox(final BoundingBox boundingBox) {
    if (!EqualsRegistry.equal(boundingBox, this.boundingBox)) {
      this.geometryFactory = boundingBox.getGeometryFactory();
      this.boundingBox = boundingBox;
      clearWarp();
      setHasChanges(true);
    }
  }

  public void setBoundingBox(final double x1, final double y1,
    final double pixelWidth, final double pixelHeight) {
    final GeometryFactory geometryFactory = getGeometryFactory();

    final int imageWidth = getImageWidth();
    final double x2 = x1 + pixelWidth * imageWidth;

    final int imageHeight = getImageHeight();
    final double y2 = y1 - pixelHeight * imageHeight;
    final BoundingBox boundingBox = new Envelope(geometryFactory, 2, x1, y1,
      x2, y2);
    setBoundingBox(boundingBox);
  }

  public void setCoordinateSystem(final CoordinateSystem coordinateSystem) {
    setGeometryFactory(GeometryFactory.getFactory(coordinateSystem));
  }

  public void setDpi(final int... dpi) {
    this.dpi = dpi;
  }

  public void setGeometryFactory(final GeometryFactory geometryFactory) {
    this.geometryFactory = geometryFactory;
  }

  protected void setHasChanges(final boolean hasChanges) {
    this.hasChanges = hasChanges;
  }

  public void setImage(final BufferedImage image) {
    this.image = image;
  }

  protected void setImageHeight(final int imageHeight) {
    this.imageHeight = imageHeight;
  }

  protected void setImageWidth(final int imageWidth) {
    this.imageWidth = imageWidth;
  }

  public void setJaiImage(final RenderedOp jaiImage) {
    this.jaiImage = jaiImage;
  }

  protected void setResolution(final double resolution) {
    this.resolution = resolution;
  }

  public void setTiePoints(final List<MappedLocation> tiePoints) {
    if (!EqualsRegistry.equal(tiePoints, this.tiePoints)) {
      for (final MappedLocation mappedLocation : this.tiePoints) {
        mappedLocation.removeListener(this);
      }
      this.tiePoints.clear();
      this.tiePoints.addAll(tiePoints);
      for (final MappedLocation mappedLocation : tiePoints) {
        mappedLocation.addListener(this);
      }
      setHasChanges(true);
      clearWarp();
    }
  }

  @Override
  public Map<String, Object> toMap() {
    final Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("type", "image");
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
    if (imageResource == null) {
      return super.toString();
    } else {
      return imageResource.toString();
    }
  }
}
