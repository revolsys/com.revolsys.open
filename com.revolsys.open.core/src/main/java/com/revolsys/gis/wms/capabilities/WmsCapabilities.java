package com.revolsys.gis.wms.capabilities;

import java.net.URL;
import java.util.List;

public class WmsCapabilities {
  private Capability capability;

  private Service service;

  private String updateSequence;

  private String version;

  public Capability getCapability() {
    return this.capability;
  }

  public WmsLayer getLayer(final String name) {
    return getLayer(this.capability.getLayer(), name);
  }

  private WmsLayer getLayer(final WmsLayer layer, final String name) {

    final String layerName = layer.getName();
    if (layerName != null && layerName.equals(name)) {
      return layer;
    }
    for (final WmsLayer childLayer : layer.getLayers()) {
      final WmsLayer matchedLayer = getLayer(childLayer, name);
      if (matchedLayer != null) {
        return matchedLayer;
      }
    }
    return null;
  }

  public Request getRequest(final String requestName) {
    for (final Request request : this.capability.getRequests()) {
      if (request.getName().equalsIgnoreCase(requestName)) {
        return request;
      }
    }
    return null;
  }

  public URL getRequestUrl(final String requestName, final String methodName) {
    final Request request = getRequest(requestName);
    if (request != null) {
      for (final DcpType type : request.getDcpTypes()) {
        if (type instanceof HttpDcpType) {
          final HttpDcpType httpType = (HttpDcpType)type;
          for (final HttpMethod httpMethod : httpType.getMethods()) {
            if (httpMethod.getName().equalsIgnoreCase(methodName)) {
              return httpMethod.getOnlineResource();
            }
          }
        }
      }
    }
    return null;
  }

  public Service getService() {
    return this.service;
  }

  public String getUpdateSequence() {
    return this.updateSequence;
  }

  public String getVersion() {
    return this.version;
  }

  public boolean hasLayer(final String name) {
    return getLayer(name) != null;
  }

  public boolean isSrsSupported(final String srsId, final List<String> layerNames) {
    final WmsLayer layer = this.capability.getLayer();
    return isSrsSupported(srsId, layer, layerNames, false);
  }

  private boolean isSrsSupported(final String srsId, final WmsLayer layer,
    final List<String> layerNames, final boolean parentHasSrs) {
    final boolean hasSrs = layer.getSrs().contains(srsId) || parentHasSrs;
    if (layerNames.contains(layer.getName())) {
      if (hasSrs) {
        return true;
      }
    }
    for (final WmsLayer childLayer : layer.getLayers()) {
      if (isSrsSupported(srsId, childLayer, layerNames, hasSrs)) {
        return true;
      }
    }
    return false;
  }

  public void setCapability(final Capability capability) {
    this.capability = capability;
  }

  public void setService(final Service service) {
    this.service = service;
  }

  public void setUpdateSequence(final String updateSequence) {
    this.updateSequence = updateSequence;
  }

  public void setVersion(final String version) {
    this.version = version;
  }
}
