package com.revolsys.record.io.format.esri.map.rest;

import com.revolsys.util.UrlUtil;

public class ArcGisServerRestClient {

  public static MapServer getMapServer(String url) {
    url = url.replaceAll("/*MapServer/*(\\?.*)?", "");
    final String baseUrl = UrlUtil.getParent(url);
    final String name = UrlUtil.getFileName(url);
    final Catalog catalog = new Catalog(baseUrl);
    final MapServer service = catalog.getService(name, MapServer.class);
    return service;
  }
}
