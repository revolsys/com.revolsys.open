package com.revolsys.swing.map.layer.bing;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.number.Doubles;
import org.jeometry.coordinatesystem.model.systems.EpsgId;

import com.revolsys.collection.map.LinkedHashMapEx;
import com.revolsys.collection.map.MapEx;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.geometry.model.Point;
import com.revolsys.raster.BufferedGeoreferencedImage;
import com.revolsys.raster.BufferedImages;
import com.revolsys.raster.GeoreferencedImage;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.UriTemplate;
import com.revolsys.util.UrlUtil;

public class BingClient {

  private static final double[] METRES_PER_PIXEL = {
    78271.517, 39135.7585, 19567.8792, 9783.9396, 4891.9698, 2445.9849, 1222.9925, 611.4962,
    305.7481, 152.8741, 76.437, 38.2185, 19.1093, 9.5546, 4.7773, 2.3887, 1.1943, 0.5972, 0.2986,
    0.1493, 0.0746, 0.0373, 0.0187
  };

  public static final int TILE_SIZE = 256;

  public static final GeometryFactory WGS84 = GeometryFactory.floating3d(EpsgId.WGS84);

  private static final GeometryFactory WORLD_MERCATOR = GeometryFactory.worldMercator();

  private final String bingMapsKey;

  private final Map<ImagerySet, Map<String, Object>> recordDefinitionCache = new HashMap<>();

  public BingClient() {
    this(null);
  }

  public BingClient(final String bingMapsKey) {
    if (Property.hasValue(bingMapsKey)) {
      this.bingMapsKey = bingMapsKey;
    } else {
      this.bingMapsKey = "Aot4lgzhMpHW2veWHlULTZEilxA69oF94eZQrA8B_C25uybJpEERRIFi7R2WI1C_";
    }
  }

  public BoundingBox getBoundingBox(final int zoomLevel, final int tileX, final int tileY) {
    final double y1 = getLatitude(zoomLevel, tileY);
    final double y2 = getLatitude(zoomLevel, tileY + 1);
    final double x1 = getLongitude(zoomLevel, tileX);
    final double x2 = getLongitude(zoomLevel, tileX + 1);
    return WGS84.newBoundingBox(x1, y1, x2, y2)//
      .bboxToCs(WORLD_MERCATOR);
  }

  public Map<String, Object> getImageryMetadata(final ImagerySet imagerySet) {
    Map<String, Object> cachedRecordDefinition = this.recordDefinitionCache.get(imagerySet);
    if (cachedRecordDefinition == null) {
      final String url = getImageryMetadataUrl(imagerySet);
      cachedRecordDefinition = Json.toMap(new UrlResource(url));
      this.recordDefinitionCache.put(imagerySet, cachedRecordDefinition);
    }
    return cachedRecordDefinition;
  }

  public String getImageryMetadataUrl(ImagerySet imagerySet) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    final MapEx parameters = newParameterMap() //
      .add("output", "json");
    return UrlUtil.getUrl("http://dev.virtualearth.net/REST/V1/Imagery/Metadata/" + imagerySet,
      parameters);
  }

  public double getLatitude(final int zoomLevel, final int tileY) {
    final double mapSize = getMapSizePixels(zoomLevel);
    final double y = 0.5 - tileY * TILE_SIZE / mapSize;

    return 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
  }

  public double getLongitude(final int zoomLevel, final int tileX) {
    final double mapSize = getMapSizePixels(zoomLevel);
    final double x = tileX * TILE_SIZE / mapSize - 0.5;
    return 360 * x;
  }

  public GeoreferencedImage getMapImage(final ImagerySet imagerySet, final MapLayer mapLayer,
    BoundingBox boundingBox, final int zoomLevel) {
    boundingBox = boundingBox //
      .bboxToCs(WORLD_MERCATOR);
    final double resolution = METRES_PER_PIXEL[zoomLevel - 1];
    final double minX = Math.floor(boundingBox.getMinX() / resolution) * resolution;
    final double minY = Math.floor(boundingBox.getMinY() / resolution) * resolution;
    final double maxX = Math.ceil(boundingBox.getMaxX() / resolution) * resolution;
    final double maxY = Math.ceil(boundingBox.getMaxY() / resolution) * resolution;
    final double mapWidth = maxX - minX;
    final double mapHeight = maxY - minY;
    final int imageWidth = Math.max(80, (int)Math.ceil(mapWidth / resolution));
    final int imageHeight = Math.max(80, (int)Math.ceil(mapHeight / resolution));

    if (imageWidth > 2000 || imageHeight > 1500) {
      final double yCount = Math.ceil(imageHeight / 1500.0);
      final double xCount = Math.ceil(imageWidth / 2000.0);
      final int yStep = (int)Math.ceil(imageHeight / yCount);
      final int xStep = (int)Math.ceil(imageWidth / xCount);
      final BufferedImage image = new BufferedImage(imageWidth, imageHeight,
        BufferedImage.TYPE_INT_ARGB);
      final Graphics graphics2d = image.getGraphics();
      for (int yIndex = 0; yIndex < imageHeight; yIndex += yStep) {
        final double y = minY + yIndex * resolution;
        final int height = Math.max(80, Math.min(imageHeight - yIndex, yStep));
        for (int xIndex = 0; xIndex < imageWidth; xIndex += xStep) {
          final double x = minX + xIndex * resolution;
          final int width = Math.max(80, Math.min(imageWidth - xIndex, xStep));
          final String url = getMapUrl(imagerySet, mapLayer, x, y, zoomLevel, width, height,
            resolution);
          final BufferedImage bufferedImage = BufferedImages.readImageIo(url);
          graphics2d.drawImage(bufferedImage, xIndex, yIndex, null);
        }
      }
      return newBufferedImage(image, minX, minY, imageWidth, imageHeight, resolution);
    } else {
      return getMapImage(imagerySet, mapLayer, minX, minY, zoomLevel, imageWidth, imageHeight,
        resolution);
    }
  }

  private GeoreferencedImage getMapImage(final ImagerySet imagerySet, final MapLayer mapLayer,
    final double minX, final double minY, final int zoomLevel, final int imageWidth,
    final int imageHeight, final double resolution) {
    final String url = getMapUrl(imagerySet, mapLayer, minX, minY, zoomLevel, imageWidth,
      imageHeight, resolution);
    final BufferedImage bufferedImage = BufferedImages.readImageIo(url);
    return newBufferedImage(bufferedImage, minX, minY, imageWidth, imageHeight, resolution);
  }

  public BufferedImage getMapImage(final ImagerySet imagerySet, final MapLayer mapLayer,
    final String quadKey) {
    final String url = getMapUrl(imagerySet, mapLayer, quadKey);
    if (url == null) {
      return null;
    } else {
      return BufferedImages.readImageIo(url);
    }
  }

  public BufferedImage getMapImage(final ImagerySet imagerySet, final MapLayer mapLayer,
    final String format, final double minX, final double minY, final double maxX, final double maxY,
    final int width, final int height) {
    final String url = getMapUrl(imagerySet, mapLayer, format, minX, minY, maxX, maxY, width,
      height);
    return BufferedImages.readImageIo(url);
  }

  public int getMapSizePixels(final int zoomLevel) {
    return TILE_SIZE << zoomLevel;
  }

  public String getMapUrl(ImagerySet imagerySet, final MapLayer mapLayer, final double minX,
    final double minY, final int zoomLevel, final int width, final int height,
    final double resolution) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }

    final MapEx parameters = newParameterMap() //
      .add("mapSize", width + "," + height) //
      .add("mapLayer", mapLayer) //
    ;

    final Point centrePoint = WORLD_MERCATOR //
      .point(minX + width * resolution / 2, minY + height * resolution / 2) //
      .as2d(WGS84);
    final double centreX = centrePoint.getX();
    final double centreY = centrePoint.getY();
    final String centre = Doubles.toString(centreY) + "," + Doubles.toString(centreX);
    return UrlUtil.getUrl("https://dev.virtualearth.net/REST/v1/Imagery/Map/" + imagerySet + "/"
      + centre + "/" + zoomLevel, parameters);
  }

  @SuppressWarnings("unchecked")
  public String getMapUrl(ImagerySet imagerySet, final MapLayer mapLayer, final String quadKey) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    final Map<String, Object> recordDefinition = getImageryMetadata(imagerySet);
    final List<Map<String, Object>> recordSets = (List<Map<String, Object>>)recordDefinition
      .get("resourceSets");
    if (recordSets == null) {
      return null;
    } else {
      final Map<String, Object> recordSet = recordSets.get(0);
      final List<Map<String, Object>> resources = (List<Map<String, Object>>)recordSet
        .get("resources");
      final Map<String, Object> resource = resources.get(0);
      final String imageUrl = (String)resource.get("imageUrl");

      final UriTemplate uriTemplate = new UriTemplate(imageUrl);

      // http://ecn.{subdomain}.tiles.virtualearth.net/tiles/r{quadkey}.jpeg?g=1173&mkt={culture}&shading=hill
      final MapEx parameters = newParameterMap();
      final Map<String, Object> templateParameters = newParameterMap();
      if (mapLayer == null) {
        templateParameters.put("culture", "");
      } else {
        templateParameters.put("culture", mapLayer);
      }
      templateParameters.put("quadkey", quadKey);
      templateParameters.put("subdomain", "t0");

      final URI uri = uriTemplate.expand(templateParameters);

      return UrlUtil.getUrl(uri, parameters);
    }

  }

  public String getMapUrl(ImagerySet imagerySet, final MapLayer mapLayer, final String format,
    final double minX, final double minY, final double maxX, final double maxY, int width,
    int height) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    if (width < 80) {
      width = 80;
    } else if (width > 2000) {
      width = 2000;
    }
    if (height < 80) {
      height = 80;
    } else if (height > 1500) {
      height = 1500;
    }
    final String mapArea = Doubles.toString(minY) + "," + Doubles.toString(minX) + ","
      + Doubles.toString(maxY) + "," + Doubles.toString(maxX);
    final String mapSize = width + "," + height;
    final MapEx parameters = newParameterMap() //
      .add("mapArea", mapArea) //
      .add("mapSize", mapSize) //
      .add("mapLayer", mapLayer) //
      .add("format", format) //
    ;

    return UrlUtil.getUrl("https://dev.virtualearth.net/REST/v1/Imagery/Map/" + imagerySet,
      parameters);
  }

  public String getQuadKey(final int zoomLevel, final int tileX, final int tileY) {
    final StringBuilder quadKey = new StringBuilder();
    for (int i = zoomLevel; i > 0; i--) {
      char digit = '0';
      final int mask = 1 << i - 1;
      if ((tileX & mask) != 0) {
        digit++;
      }
      if ((tileY & mask) != 0) {
        digit++;
        digit++;
      }
      quadKey.append(digit);
    }
    return quadKey.toString();
  }

  public double getResolution(final int zoomLevel) {
    final int zoomIndex = Math.min(zoomLevel - 1, METRES_PER_PIXEL.length - 1);
    return METRES_PER_PIXEL[zoomIndex];
  }

  public int getTileX(final int zoomLevel, final double longitude) {
    final double ratio = (longitude + 180) / 360;
    int tileX = (int)Math.floor(ratio * (1 << zoomLevel));

    if (ratio >= 1) {
      tileX--;
    }
    return tileX;
  }

  public int getTileY(final int zoomLevel, final double latitude) {
    final double sinLatitude = Math.sin(latitude * Math.PI / 180);
    final int tileY = (int)Math
      .floor((0.5 - Math.log((1 + sinLatitude) / (1 - sinLatitude)) / (4 * Math.PI))
        * Math.pow(2, zoomLevel));
    return tileY;
  }

  public int getZoomLevel(final ImagerySet imagerySet, final double resolution) {
    for (int i = 0; i < imagerySet.getMaxLevelOfDetail(); i++) {
      final double levelResolution = METRES_PER_PIXEL[i];
      if (resolution > levelResolution) {
        if (i == 0) {
          return 1;
        } else {
          final double ratio = levelResolution / resolution;
          if (ratio < 0.95) {
            return i;
          } else {
            return i + 1;
          }
        }
      }
    }
    return imagerySet.getMaxLevelOfDetail();
  }

  private BufferedGeoreferencedImage newBufferedImage(final BufferedImage image, final double minX,
    final double minY, final int imageWidth, final int imageHeight, final double resolution) {
    final double maxX = minX + imageWidth * resolution;
    final double maxY = minY + imageHeight * resolution;
    final BoundingBox boundingBox = WORLD_MERCATOR.newBoundingBox(minX, minY, maxX, maxY);
    return new BufferedGeoreferencedImage(boundingBox, image);
  }

  private MapEx newParameterMap() {
    return new LinkedHashMapEx("key", this.bingMapsKey);
  }
}
