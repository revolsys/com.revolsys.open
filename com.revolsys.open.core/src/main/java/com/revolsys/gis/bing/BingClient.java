package com.revolsys.gis.bing;

import java.util.Map;
import java.util.TreeMap;

import com.revolsys.util.UrlUtil;

public class BingClient {
  public enum ImagerySet {
    Aerial, AerialWithLabels, Road, OrdnanceSurvey, CollinsBart
  }
public enum MapLayer {
  TrafficFlow
}
  private String bingMapsKey;

  public BingClient(String bingMapsKey) {
    this.bingMapsKey = bingMapsKey;
  }

  public String get(ImagerySet imagerySet, MapLayer mapLayer, String format,
    double minX, double minY, double maxX, double maxY, Integer width,
    Integer height) {
    if (imagerySet == null) {
      imagerySet = ImagerySet.Aerial;
    }
    if (width == null) {
      width = 350;
    } else if (width < 80 || width > 900) {
      throw new IllegalArgumentException("Width must be between 80-900");
    }
    if (height == null) {
      height = 350;
    } else if (height < 80 || height > 834) {
      throw new IllegalArgumentException("Height must be between 80-834");
    }
    Map<String, Object> parameters = createParameterMap();
    parameters.put("mapArea", minY + "," + minX + "," + maxY + "," + maxY);
    parameters.put("mapSize", width + "," + height);
    parameters.put("mapLayer", mapLayer);
    parameters.put("format", format);

    return UrlUtil.getUrl("http://dev.virtualearth.net/REST/v1/Imagery/Map/"
      + imagerySet, parameters);
  }

  private Map<String, Object> createParameterMap() {
    Map<String, Object> parameters = new TreeMap<String, Object>();
    parameters.put("key", bingMapsKey);
    return parameters;
  }
}
