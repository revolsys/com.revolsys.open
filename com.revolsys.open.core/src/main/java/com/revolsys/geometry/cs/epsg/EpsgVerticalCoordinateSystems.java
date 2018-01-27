package com.revolsys.geometry.cs.epsg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.IntHashMap;
import com.revolsys.geometry.cs.VerticalCoordinateSystem;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.code.CodeTable;

public class EpsgVerticalCoordinateSystems implements CodeTable {
  public static final EpsgVerticalCoordinateSystems COORDINATE_SYSTEMS = new EpsgVerticalCoordinateSystems();

  public static List<VerticalCoordinateSystem> all() {
    return Collections.unmodifiableList(COORDINATE_SYSTEMS.coordinateSystems);
  }

  public static EpsgVerticalCoordinateSystems instance() {
    return COORDINATE_SYSTEMS;
  }

  private final Map<Identifier, List<Object>> codes = new HashMap<>();

  private final IntHashMap<VerticalCoordinateSystem> coordinateSystemById = new IntHashMap<>();

  private final List<VerticalCoordinateSystem> coordinateSystems = new ArrayList<>();

  private EpsgVerticalCoordinateSystems() {
    addCoordinateSystem(5713); // CGVD28
    addCoordinateSystem(6647); // CGVD2013
  }

  private void addCoordinateSystem(final int id) {
    final VerticalCoordinateSystem coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(id);
    this.coordinateSystems.add(coordinateSystem);
    final List<Object> codeList = Collections.singletonList(coordinateSystem);
    this.codes.put(coordinateSystem, codeList);
    final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
    this.coordinateSystemById.put(coordinateSystemId, coordinateSystem);
  }

  @Override
  public Map<Identifier, List<Object>> getCodes() {
    return this.codes;
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public List<Identifier> getIdentifiers() {
    return (List)Collections.unmodifiableList(this.coordinateSystems);
  }

  @Override
  public String getIdFieldName() {
    return "coordinateSystemId";
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Identifier id) {
    final int coordinateSystemId = id.getInteger(0);
    return (V)this.coordinateSystemById.get(coordinateSystemId);
  }

}
