package com.revolsys.gis.wms.capabilities;

import java.net.URL;
import java.util.List;

public class WmsCapabilities {
  private String version;

  private String updateSequence;

  private Service service;

  private Capability capability;

  public Capability getCapability() {
    return capability;
  }

  public WmsLayer getLayer(final String name) {
    return getLayer(capability.getLayer(), name);
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
    for (final Request request : capability.getRequests()) {
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
    return service;
  }

  public String getUpdateSequence() {
    return updateSequence;
  }

  public String getVersion() {
    return version;
  }

  public boolean hasLayer(final String name) {
    return getLayer(name) != null;
  }

  public boolean isSrsSupported(final String srsId,
    final List<String> layerNames) {
    final WmsLayer layer = capability.getLayer();
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
