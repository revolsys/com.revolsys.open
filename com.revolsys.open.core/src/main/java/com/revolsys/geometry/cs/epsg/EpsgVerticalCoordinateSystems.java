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

  public static final VerticalCoordinateSystem CGVD2013 = byId(6647);

  public static final VerticalCoordinateSystem CGVD28 = byId(5713);

  public static List<EpsgVerticalCoordinateSystem> all() {
    return COORDINATE_SYSTEMS.getCoordinateSystems();
  }

  public static EpsgVerticalCoordinateSystem byId(final int coordinateSystemId) {
    return COORDINATE_SYSTEMS.getCoordinateSystem(coordinateSystemId);
  }

  public static EpsgVerticalCoordinateSystems instance() {
    return COORDINATE_SYSTEMS;
  }

  public static String nameById(final int coordinateSystemId) {
    final EpsgVerticalCoordinateSystem coordinateSystem = COORDINATE_SYSTEMS
      .getCoordinateSystem(coordinateSystemId);
    if (coordinateSystem == null) {
      return null;
    } else {
      return coordinateSystem.getCoordinateSystemName();
    }
  }

  private final Map<Identifier, List<Object>> codes = new HashMap<>();

  private final IntHashMap<EpsgVerticalCoordinateSystem> coordinateSystemById = new IntHashMap<>();

  private final List<EpsgVerticalCoordinateSystem> coordinateSystems = new ArrayList<>();

  private EpsgVerticalCoordinateSystems() {
    addCoordinateSystem(5713, "CGVD28 height", "Canadian Geodetic Vertical Datum of 1928");
    addCoordinateSystem(6647, "CGVD2013 height", "Canadian Geodetic Vertical Datum of 2013");
  }

  private void addCoordinateSystem(final EpsgVerticalCoordinateSystem coordinateSystem) {
    this.coordinateSystems.add(coordinateSystem);
    final List<Object> codeList = Collections.singletonList(coordinateSystem);
    this.codes.put(coordinateSystem, codeList);
    final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
    this.coordinateSystemById.put(coordinateSystemId, coordinateSystem);
  }

  private void addCoordinateSystem(final int id, final String name, final String datumName) {
    final EpsgVerticalCoordinateSystem coordinateSystem = new EpsgVerticalCoordinateSystem(id, name,
      datumName);
    addCoordinateSystem(coordinateSystem);
  }

  @Override
  public Map<Identifier, List<Object>> getCodes() {
    return this.codes;
  }

  public EpsgVerticalCoordinateSystem getCoordinateSystem(final int coordinateSystemId) {
    return this.coordinateSystemById.get(coordinateSystemId);
  }

  public List<EpsgVerticalCoordinateSystem> getCoordinateSystems() {
    return Collections.unmodifiableList(this.coordinateSystems);
  }

  @SuppressWarnings({
    "unchecked", "rawtypes"
  })
  @Override
  public List<Identifier> getIdentifiers() {
    return (List)getCoordinateSystems();
  }

  @Override
  public String getIdFieldName() {
    return "coordinateSystemId";
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Identifier id) {
    final int coordinateSystemId = id.getInteger(0);
    return (V)getCoordinateSystem(coordinateSystemId);
  }

}
