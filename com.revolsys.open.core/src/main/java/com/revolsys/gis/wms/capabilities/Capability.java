package com.revolsys.gis.wms.capabilities;

import java.util.ArrayList;
import java.util.List;

public class Capability {

  private List<Request> requests = new ArrayList<Request>();

  private final List<String> exceptionFormats = new ArrayList<String>();

  private Layer layer;

  public void addExceptionFormat(final String format) {
    exceptionFormats.add(format);
  }

  public void addRequest(final Request request) {
    requests.add(request);

  }

  public List<String> getExceptionFormats() {
    return exceptionFormats;
  }

  public Layer getLayer() {
    return layer;
  }

  public List<Request> getRequests() {
    return requests;
  }

  public void setLayer(final Layer layer) {
    this.layer = layer;
  }

  public void setRequests(final List<Request> requests) {
    this.requests = requests;
  }
}
