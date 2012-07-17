package com.revolsys.io.esri.map.rest.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.revolsys.io.esri.map.rest.AbstractMapWrapper;

public class LayerDescription extends AbstractMapWrapper {
  public LayerDescription() {
  }

  public boolean getDefaultVisibility() {
    return getValue("defaultVisibility");
  }

  public Integer getId() {
    return getIntValue("id");
  }

  public Double getMaxScale() {
    return getDoubleValue("maxScale");
  }

  public Double getMinScale() {
    return getDoubleValue("minScale");
  }

  public String getName() {
    return getValue("name");
  }

  public Integer getParentLayerId() {
    return getIntValue("parentLayerId");
  }

  public List<Integer> getSubLayerIds() {
    final List<Integer> subLayerIds = new ArrayList<Integer>();

    final List<Number> ids = getValue("subLayerIds");
    if (ids != null) {
      for (final Number layerId : ids) {
        subLayerIds.add(layerId.intValue());
      }
    }
    return subLayerIds;
  }

  @Override
  public String toString() {
    return getName();
  }
}
