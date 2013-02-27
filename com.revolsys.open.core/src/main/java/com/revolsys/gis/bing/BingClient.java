package com.revolsys.gis.bing;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.springframework.core.io.UrlResource;

import com.revolsys.converter.string.StringConverterRegistry;
import com.revolsys.gis.cs.BoundingBox;
import com.revolsys.gis.cs.GeometryFactory;
import com.revolsys.io.json.JsonMapIoFactory;
import com.revolsys.util.UriTemplate;
import com.revolsys.util.UrlUtil;

public class BingClient {
  public enum ImagerySet {
    Aerial, AerialWithLabels, Road, OrdnanceSurvey, CollinsBart
  }

  public enum MapLayer {
    TrafficFlow
  }

  public static final GeometryFactory WORLD_MERCATOR = GeometryFactory.getFactory(3857);

  public static final GeometryFactory WGS84 = GeometryFactory.getFactory(4326);

  private static final double[] METRES_PER_PIXEL = {
    78271.517, 39135.7585, 19567.8792, 9783.9396, 4891.9698, 2445.9849,
    1222.9925, 611.4962, 305.7481, 152.8741, 76.437, 38.2185, 19.1093, 9.5546,
    4.7773, 2.3887, 1.1943, 0.5972, 0.2986, 0.1493, 0.0746
  };

  public static BoundingBox getBoundingBox(final int zoomLevel,
    final int tileX, final int tileY) {
    final double y1 = BingClient.getLatitude(zoomLevel, tileY);
    final double y2 = BingClient.getLatitude(zoomLevel, tileY + 1);
    final double x1 = BingClient.getLongitude(zoomLevel, tileX);
    final double x2 = BingClient.getLongitude(zoomLevel, tileX + 1);
    return new BoundingBox(WGS84, x1, y1, x2, y2).convert(WORLD_MERCATOR);
  }

  public static double getLatitude(final int zoomLevel, final int tileY) {
    final double mapSize = getMapSizePixels(zoomLevel);
    final double y = 0.5 - tileY * 256 / mapSize;

    return 90 - 360 * Math.atan(Math.exp(-y * 2 * Math.PI)) / Math.PI;
  }

  public static double getLongitude(final int zoomLevel, final int tileX) {
    final double mapSize = getMapSizePixels(zoomLevel);
    final double x = tileX * 256 / mapSize - 0.5;
    return 360 * x;
  }

  public static int getMapSizePixels(final int zoomLevel) {
    return 256 << zoomLevel;
  }

  public static String getQuadKey(final int zoomLevel, final int tileX,
    final int tileY) {
    final StringBuilder quadKey = new StringBuilder();
    for (int i = zoomLevel; i > 0; i--) {
      char digit = '0';
      final int mask = 1 << (i - 1);
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

  public static int getTileX(final int zoomLevel, final double longitude) {

    final double ratio = (longitude + 180) / 360;
    int tileX = (int)Math.floor(ratio * Math.pow(2, zoomLevel));
    if (ratio >= 1) {
      tileX--;
    }
    return tileX;
  }

  public static int getTileY(final int zoomLevel, final double latitude) {
    final double sinLatitude = Math.sin(latitude * Math.PI / 180);
    final int tileY = (int)Math.floor((0.5 - Math.log((1 + sinLatitude)
      / (1 - sinLatitude))
      / (4 * Math.PI))
      * Math.pow(2, zoomLevel));
    return tileY;
  }

  public static int getZoomLevel(final double metresPerPixel) {
    for (int i = 0; i < METRES_PER_PIXEL.length; i++) {
      final double zoomLevelMetresPerPixel = METRES_PER_PIXEL[i];
      if (metresPerPixel > zoomLevelMetresPerPixel) {
        return Math.max(i, 1);
      }
    }
    return METRES_PER_PIXEL.length;
  }

  private final String bingMapsKey;

  private final Map<ImagerySet, Map<String, Object>> metaDataCache = new HashMap<BingClient.ImagerySet, Map<String, Object>>();

  public BingClient(final String bingMapsKey) {
    this.bingMapsKey = bingMapsKey;
  }

  private Map<String, Object> createParameterMap() {
    final Map<String, Object> parameters = new TreeMap<String, Object>();
    parameters.put("key", bingMapsKey);
    return parameters;
  }

  public Map<String, Object> getImageryMetadata(final ImagerySet imagerySet) {
    Map<String, Object> cachedMetaData = metaDataCache.get(imagerySet);
    if (cachedMetaData == null) {
      final String url = getImageryMetadataUrl(imagerySet);
      try {
        cachedMetaData = JsonMapIoFactory.toMap(new UrlResource(url));
        metaDataCache.put(imagerySet, cachedMetaData);
      } catch (final MalformedURLException e) {
        return Collections.emptyMap();
      }
    }
    return cachedMetaData;
  }

  public String getImageryMetadataUrl(ImagerySet imagerySet) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    final Map<String, Object> parameters = createParameterMap();
    parameters.put("output", "json");
    return UrlUtil.getUrl(
      "http://dev.virtualearth.net/REST/V1/Imagery/Metadata/" + imagerySet,
      parameters);
  }

  public Image getMapImage(final ImagerySet imagerySet,
    final MapLayer mapLayer, final String quadKey) {
    final String url = getMapUrl(imagerySet, mapLayer, quadKey);
    try {
      final URLConnection connection = new URL(url).openConnection();
      final InputStream in = connection.getInputStream();
      return ImageIO.read(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to download image from: " + url, e);
    }
  }

  public Image getMapImage(final ImagerySet imagerySet,
    final MapLayer mapLayer, final String format, final double minX,
    final double minY, final double maxX, final double maxY,
    final Integer width, final Integer height, final double scale) {
    final String url = getMapUrl(imagerySet, mapLayer, format, minX, minY,
      maxX, maxY, width, height);
    try {
      final URLConnection connection = new URL(url).openConnection();
      final InputStream in = connection.getInputStream();
      return ImageIO.read(in);
    } catch (final IOException e) {
      throw new RuntimeException("Unable to download image from: " + url, e);
    }
  }

  public String getMapUrl(ImagerySet imagerySet, final MapLayer mapLayer,
    final String quadKey) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    Map<String, Object> metaData = getImageryMetadata(imagerySet);
    List<Map<String, Object>> recordSets = (List<Map<String, Object>>)metaData.get("resourceSets");
    Map<String, Object> recordSet = recordSets.get(0);
    List<Map<String, Object>> resources = (List<Map<String, Object>>)recordSet.get("resources");
    Map<String, Object> resource = resources.get(0);
    String imageUrl = (String)resource.get("imageUrl");

    UriTemplate uriTemplate = new UriTemplate(imageUrl);

    // http://ecn.{subdomain}.tiles.virtualearth.net/tiles/r{quadkey}.jpeg?g=1173&mkt={culture}&shading=hill
    final Map<String, Object> parameters = createParameterMap();
    final Map<String, Object> templateParameters = createParameterMap();
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

  public String getMapUrl(ImagerySet imagerySet, final MapLayer mapLayer,
    final String format, final double minX, final double minY,
    final double maxX, final double maxY, Integer width, Integer height) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    if (width == null) {
      width = 350;
    } else if (width < 80 || width > 900) {
      throw new IllegalArgumentException("Width must be between 80-900 not "
        + width);
    }
    if (height == null) {
      height = 350;
    } else if (height < 80 || height > 834) {
      throw new IllegalArgumentException("Height must be between 80-834 not "
        + height);
    }
    final double centreX = minX + (maxX - minX) / 2;
    final double centreY = minY + (maxY - minY) / 2;
    final Map<String, Object> parameters = createParameterMap();
    parameters.put(
      "mapArea",
      StringConverterRegistry.toString(minY) + ","
        + StringConverterRegistry.toString(minX) + ","
        + StringConverterRegistry.toString(maxY) + ","
        + StringConverterRegistry.toString(maxX));
    parameters.put("mapSize", width + "," + height);
    parameters.put("mapLayer", mapLayer);
    parameters.put("format", format);

    return UrlUtil.getUrl("http://dev.virtualearth.net/REST/v1/Imagery/Map/"
      + imagerySet + "/" + StringConverterRegistry.toString(centreY) + ","
      + StringConverterRegistry.toString(centreX), parameters);
  }
}
