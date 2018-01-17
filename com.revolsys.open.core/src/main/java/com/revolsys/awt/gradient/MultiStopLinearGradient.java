package com.revolsys.awt.gradient;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapSerializer;
import com.revolsys.properties.BaseObjectWithProperties;

public class MultiStopLinearGradient extends BaseObjectWithProperties
  implements LinearGradient, MapSerializer {

  private List<GradientStop> stops = new ArrayList<>();

  private double valueMin;

  private double valueMax;

  private int stopCount;

  private double valueRange;

  public MultiStopLinearGradient() {
    this(new ArrayList<>());
  }

  public MultiStopLinearGradient(final GradientStop... stops) {
    this.stops = Arrays.asList(stops);
  }

  public MultiStopLinearGradient(final List<GradientStop> stops) {
    this.stops = stops;
  }

  public MultiStopLinearGradient(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  @Override
  public MultiStopLinearGradient clone() {
    final MultiStopLinearGradient clone = (MultiStopLinearGradient)super.clone();
    clone.stops = new ArrayList<>();
    for (final GradientStop stop : this.stops) {
      clone.stops.add(stop.clone());
    }
    return clone;
  }

  @Override
  public int getColorIntForValue(final double value) {
    if (Double.isFinite(value)) {
      for (final GradientStop stop : this.stops) {
        final int color = stop.getValueFast(value);
        if (color != NULL_COLOR) {
          return color;
        }
      }
      return this.stops.get(this.stopCount - 1).getMaxColourInt();
    } else {
      return NULL_COLOR;
    }
  }

  public List<GradientStop> getStops() {
    return this.stops;
  }

  public void removeStop(final int index) {
    this.stops.remove(index);
  }

  public void setStops(final List<GradientStop> stops) {
    this.stops = stops;
    updateValues();
  }

  @Override
  public void setValueMax(final double valueMax) {
    this.valueMax = valueMax;
    updateValues();
  }

  @Override
  public void setValueMin(final double valueMin) {
    this.valueMin = valueMin;
    updateValues();
  }

  @Override
  public MapEx toMap() {
    final MapEx map = newTypeMap("multiStopLinearGradient");
    addToMap(map, "stops", this.stops);
    addToMap(map, "valueMin", this.valueMin);
    addToMap(map, "valueMax", this.valueMax);
    return map;
  }

  @Override
  public void updateValues() {
    this.valueRange = this.valueMax - this.valueMin;
    if (!Double.isFinite(this.valueRange)) {
      this.valueRange = 0;
    }
    this.stopCount = this.stops.size();
    Collections.sort(this.stops);
    for (int i = 0; i < this.stopCount - 1; i++) {
      final GradientStop range = this.stops.get(i);
      final GradientStop rangeNext = this.stops.get(i + 1);
      final double percent = range.getPercent();
      final double nextPercent = rangeNext.getPercent();
      final double stopValueMin = this.valueMin + this.valueRange * percent;
      final double stopValueMax = this.valueMin + this.valueRange * nextPercent;
      final Color maxColor = rangeNext.getColor();
      range.setMinMax(stopValueMin, stopValueMax, maxColor);
    }
  }
}
