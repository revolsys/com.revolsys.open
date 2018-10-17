package com.revolsys.elevation.cloud.las;

import java.util.Map;

import com.revolsys.collection.map.Maps;

public interface LasClassification {

  short GROUND = (short)2;
  Map<Short, String> CLASSIFICATIONS = Maps.<Short, String> buildLinkedHash()
  .add((short)0, "Created, never classified") //
  .add((short)1, "Unclassified") //
  .add(GROUND, "Ground") //
  .add((short)3, "Low Vegitation") //
  .add((short)4, "Medium Vegitation") //
  .add((short)5, "High Vegitation") //
  .add((short)6, "Building") //
  .add((short)7, "Low Point (noise)") //
  .add((short)8, "Model Key-point (mass point)") //
  .add((short)9, "Water") //
  .add((short)12, "Overlap Points") //
  .getMap();

}
