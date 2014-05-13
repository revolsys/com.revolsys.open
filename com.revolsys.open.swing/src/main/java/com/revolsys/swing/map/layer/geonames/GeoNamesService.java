package com.revolsys.swing.map.layer.geonames;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.measure.unit.SI;

import com.revolsys.gis.cs.GeographicCoordinateSystem;
import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectMetaData;
import com.revolsys.gis.data.model.DataObjectMetaDataImpl;
import com.revolsys.gis.data.model.types.DataTypes;
import com.revolsys.gis.model.coordinates.DoubleCoordinates;
import com.revolsys.io.PathUtil;
import com.revolsys.io.json.JsonParser;
import com.revolsys.jts.geom.BoundingBox;
import com.revolsys.jts.geom.Point;
import com.revolsys.jts.geom.GeometryFactory;
import com.revolsys.util.UrlUtil;

public class GeoNamesService {
  public static final DataObjectMetaData NAME_METADATA;

  public static final DataObjectMetaData WIKIPEDIA_METADATA;

  static {
    final DataObjectMetaDataImpl meta = new DataObjectMetaDataImpl(
      PathUtil.toPath("/geoname.org", "name"));
    meta.addAttribute("geonameId", DataTypes.STRING, false);
    meta.addAttribute("name", DataTypes.STRING, false);
    meta.addAttribute("fcode", DataTypes.STRING, false);
    meta.addAttribute("fcodeName", DataTypes.STRING, false);
    meta.addAttribute("fcl", DataTypes.STRING, false);
    meta.addAttribute("fclName", DataTypes.STRING, false);
    meta.addAttribute("adminName1", DataTypes.STRING, false);
    meta.addAttribute("adminName2", DataTypes.STRING, false);
    meta.addAttribute("adminName3", DataTypes.STRING, false);
    meta.addAttribute("adminName4", DataTypes.STRING, false);
    meta.addAttribute("adminCode1", DataTypes.STRING, false);
    meta.addAttribute("population", DataTypes.INTEGER, false);
    meta.addAttribute("countryCode", DataTypes.STRING, false);
    meta.addAttribute("countryName", DataTypes.STRING, false);
    meta.addAttribute("timeZoneId", DataTypes.STRING, false);
    meta.addAttribute("geometry", DataTypes.POINT, false);
    NAME_METADATA = meta;

    final DataObjectMetaDataImpl wikipediaMetaData = new DataObjectMetaDataImpl(
      PathUtil.toPath("/geoname.org", "wikipedia"));
    wikipediaMetaData.addAttribute("summary", DataTypes.STRING, false);
    wikipediaMetaData.addAttribute("title", DataTypes.STRING, false);
    wikipediaMetaData.addAttribute("wikipediaUrl", DataTypes.STRING, false);
    wikipediaMetaData.addAttribute("countryCode", DataTypes.STRING, false);
    wikipediaMetaData.addAttribute("feature", DataTypes.STRING, false);
    wikipediaMetaData.addAttribute("thumbnailImg", DataTypes.STRING, false);
    wikipediaMetaData.addAttribute("lang", DataTypes.STRING, false);
    wikipediaMetaData.addAttribute("population", DataTypes.INTEGER, false);
    wikipediaMetaData.addAttribute("geometry", DataTypes.POINT, false);

    WIKIPEDIA_METADATA = wikipediaMetaData;
  }

  private URL searchJsonUrl;

  private URL wikipediaBoundingBoxJsonUrl;

  private URL findNearbyBoundingBoxJsonUrl;

  public GeoNamesService() {
    final String url = "Http://ws.geonames.org";
    try {
      init(new URL(url));
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL:" + url);
    }
  }

  public GeoNamesService(final URL url) {
    init(url);
  }

  public List<DataObject> getNames(final BoundingBox boundingBox) {
    final GeometryFactory geometryFactory = GeometryFactory.getFactory(4326);
    final GeographicCoordinateSystem cs = (GeographicCoordinateSystem)geometryFactory.getCoordinateSystem();
    final BoundingBox geographicBoundingBox = boundingBox.convert(geometryFactory);
    final Map<String, Object> params = new HashMap<String, Object>();

    final double radius = cs.getDatum().getSpheroid().getSemiMajorAxis();
    final double height = geographicBoundingBox.getHeight();
    final double width = geographicBoundingBox.getWidth();
    final double diagonal = Math.sqrt(width * width + height * height);
    final double radiusKm = cs.getUnit()
      .getConverterTo(SI.RADIAN)
      .convert(diagonal)
      * radius / 1000;

    params.put("lat", geographicBoundingBox.getCentreY());
    params.put("lng", geographicBoundingBox.getCentreX());
    params.put("radius", radiusKm);
    params.put("maxRows", "50");
    final String searchUrlString = UrlUtil.getUrl(
      this.findNearbyBoundingBoxJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(NAME_METADATA, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:"
        + searchUrlString);
    }
  }

  public List<DataObject> getWikipediaArticles(final BoundingBox boundingBox) {
    final BoundingBox geographicBoundingBox = boundingBox.convert(GeometryFactory.getFactory(4326));
    final Map<String, Object> params = new HashMap<String, Object>();

    params.put("north", geographicBoundingBox.getMaxY());
    params.put("east", geographicBoundingBox.getMaxX());
    params.put("south", geographicBoundingBox.getMinY());
    params.put("west", geographicBoundingBox.getMinX());
    params.put("maxRows", "50");
    final String searchUrlString = UrlUtil.getUrl(
      this.wikipediaBoundingBoxJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(WIKIPEDIA_METADATA, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:"
        + searchUrlString);
    }
  }

  private void init(final URL url) {
    try {
      this.searchJsonUrl = new URL(url.toString() + "/searchJSON");
      this.wikipediaBoundingBoxJsonUrl = new URL(url.toString()
        + "/wikipediaBoundingBoxJSON");
      this.findNearbyBoundingBoxJsonUrl = new URL(url.toString()
        + "/findNearbyJSON");
    } catch (final MalformedURLException e) {
      throw new IllegalArgumentException("Invalid URL:" + url);
    }
  }

  private List<DataObject> mapToObjects(final DataObjectMetaData metaData,
    final Map<String, Object> result) {
    final List<DataObject> results = new ArrayList<DataObject>();
    final List<Map<String, Object>> names = (List<Map<String, Object>>)result.get("geonames");
    for (final Map<String, Object> name : names) {
      final DataObject dataObject = metaData.createDataObject();
      for (final String attributeName : metaData.getAttributeNames()) {
        final Object value = name.get(attributeName);
        if (value != null) {
          dataObject.setValue(attributeName, value);
        }
      }
      final double lat = ((Number)name.get("lat")).doubleValue();
      final double lon = ((Number)name.get("lng")).doubleValue();

      Point coordinate = new DoubleCoordinates(lon, lat);
      final Number elevation = (Number)name.get("elevation");
      if (elevation == null) {
        coordinate = new DoubleCoordinates(lon, lat);
      } else {
        coordinate = new DoubleCoordinates(lon, lat, elevation.doubleValue());
      }
      dataObject.setGeometryValue(GeometryFactory.getFactory()
        .point(coordinate));
      results.add(dataObject);
    }
    return results;
  }

  public List<DataObject> searchByName(final String name) {
    return searchByName(name, null);
  }

  public List<DataObject> searchByName(final String name,
    final String countryCode) {
    final Map<String, String> params = new HashMap<String, String>();
    params.put("name", name);
    params.put("style", "FULL");
    params.put("type", "json");
    final String searchUrlString = UrlUtil.getUrl(this.searchJsonUrl, params);
    try {
      final URL searchUrl = new URL(searchUrlString);
      final Map<String, Object> result = JsonParser.getMap(searchUrl.openStream());
      return mapToObjects(NAME_METADATA, result);
    } catch (final IOException e) {
      throw new IllegalArgumentException("Unable to connect to URL:"
        + searchUrlString);
    }
  }
}
