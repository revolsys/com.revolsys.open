package com.revolsys.io.esri.map.rest;

import com.revolsys.util.UrlUtil;

public class ArcGisServerRestClient {

  public static MapServer getMapServer(String url) {
    url = url.replaceAll("/*MapServer/*(\\?.*)?", "");
    String baseUrl = UrlUtil.getParent(url);
    String name = UrlUtil.getFileName(url);
    Catalog catalog = new Catalog(baseUrl);
    MapServer service = catalog.getService(name, MapServer.class);
    return service;
  }
}
