package com.revolsys.swing.map.layer.bing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import com.revolsys.datatype.DataTypes;
import com.revolsys.geometry.model.BoundingBox;
import com.revolsys.geometry.model.GeometryFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.spring.resource.UrlResource;
import com.revolsys.util.Property;
import com.revolsys.util.UriTemplate;
import com.revolsys.util.UrlUtil;

public class BingClient {

  private static final double[] METRES_PER_PIXEL = {
    78271.517, 39135.7585, 19567.8792, 9783.9396, 4891.9698, 2445.9849, 1222.9925, 611.4962,
    305.7481, 152.8741, 76.437, 38.2185, 19.1093, 9.5546, 4.7773, 2.3887, 1.1943, 0.5972, 0.2986,
    0.1493, 0.0746
  };

  public static final int TILE_SIZE = 256;

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
    return GeometryFactory.wgs84().newBoundingBox(x1, y1, x2, y2).convert(
      GeometryFactory.worldMercator());
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
    final Map<String, Object> parameters = newParameterMap();
    parameters.put("output", "json");
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

  public BufferedImage getMapImage(final ImagerySet imagerySet, final MapLayer mapLayer,
    final String quadKey) {
    final String url = getMapUrl(imagerySet, mapLayer, quadKey);
    if (url == null) {
      return null;
    } else {
      try {
        final URLConnection connection = new URL(url).openConnection();
        final InputStream in = connection.getInputStream();
        return ImageIO.read(in);
      } catch (final IOException e) {
        throw new RuntimeException("Unable to download image from: " + url, e);
      }
    }
  }

  public Image getMapImage(final ImagerySet imagerySet, final MapLayer mapLayer,
    final String format, final double minX, final double minY, final double maxX, final double maxY,
    final Integer width, final Integer height, final double scale) {
    final String url = getMapUrl(imagerySet, mapLayer, format, minX, minY, maxX, maxY, width,
      height);
    try {
      final URLConnection connection = new URL(url).openConnection();
      final InputStream in = connection.getInputStream();
      return ImageIO.read(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to download image from: " + url, e);
    }
  }

  public int getMapSizePixels(final int zoomLevel) {
    return TILE_SIZE << zoomLevel;
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
      final Map<String, Object> parameters = newParameterMap();
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
    final double minX, final double minY, final double maxX, final double maxY, Integer width,
    Integer height) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    if (width == null) {
      width = 350;
    } else if (width < 80 || width > 900) {
      throw new IllegalArgumentException("Width must be between 80-900 not " + width);
    }
    if (height == null) {
      height = 350;
    } else if (height < 80 || height > 834) {
      throw new IllegalArgumentException("Height must be between 80-834 not " + height);
    }
    final double centreX = minX + (maxX - minX) / 2;
    final double centreY = minY + (maxY - minY) / 2;
    final Map<String, Object> parameters = newParameterMap();
    parameters.put("mapArea", DataTypes.toString(minY) + "," + DataTypes.toString(minX) + ","
      + DataTypes.toString(maxY) + "," + DataTypes.toString(maxX));
    parameters.put("mapSize", width + "," + height);
    parameters.put("mapLayer", mapLayer);
    parameters.put("format", format);

    return UrlUtil.getUrl("http://dev.virtualearth.net/REST/v1/Imagery/Map/" + imagerySet + "/"
      + DataTypes.toString(centreY) + "," + DataTypes.toString(centreX), parameters);
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
    return METRES_PER_PIXEL[Math.min(zoomLevel, METRES_PER_PIXEL.length - 1)];
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

  public int getZoomLevel(final double metresPerPixel) {
    for (int i = 0; i < METRES_PER_PIXEL.length; i++) {
      final double zoomLevelMetresPerPixel = METRES_PER_PIXEL[i];
      if (metresPerPixel > zoomLevelMetresPerPixel) {
        return Math.max(i, 1);
      }
    }
    return METRES_PER_PIXEL.length;
  }

  private Map<String, Object> newParameterMap() {
    final Map<String, Object> parameters = new TreeMap<>();
    parameters.put("key", this.bingMapsKey);
    return parameters;
  }
}
