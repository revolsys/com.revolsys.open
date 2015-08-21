package com.revolsys.format.openstreetmap.model;

import java.util.Arrays;
import java.util.List;

import javax.xml.namespace.QName;

import com.revolsys.geometry.model.GeometryFactory;

public interface OsmConstants {
  QName BOUNDS = new QName("bounds");

  QName ND = new QName("nd");

  QName NODE = new QName("node");

  String OSM = "osm";

  QName TAG = new QName("tag");

  QName RELATION = new QName("relation");

  QName WAY = new QName("way");

  GeometryFactory WGS84_2D = GeometryFactory.floating(4326, 2);

  List<QName> NODE_XML_ELEMENTS = Arrays.asList(TAG);

  List<QName> WAY_XML_ELEMENTS = Arrays.asList(TAG, ND);

  List<QName> RELATION_XML_ELEMENTS = Arrays.asList(TAG);

  List<QName> OSM_XML_ELEMENTS = Arrays.asList(BOUNDS, NODE, WAY, RELATION);
}
