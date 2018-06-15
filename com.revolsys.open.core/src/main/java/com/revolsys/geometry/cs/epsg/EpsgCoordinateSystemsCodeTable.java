package com.revolsys.geometry.cs.epsg;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.geometry.cs.CoordinateSystem;
import com.revolsys.geometry.cs.HorizontalCoordinateSystem;
import com.revolsys.geometry.cs.VerticalCoordinateSystem;
import com.revolsys.identifier.Identifier;
import com.revolsys.record.code.CodeTable;

public class EpsgCoordinateSystemsCodeTable implements CodeTable {

  private static Reference<EpsgCoordinateSystemsCodeTable> verticalReference = new WeakReference<>(
    null);

  private static Reference<EpsgCoordinateSystemsCodeTable> horizontalReference = new WeakReference<>(
    null);

  public static EpsgCoordinateSystemsCodeTable horizontal() {
    EpsgCoordinateSystemsCodeTable codeTable = horizontalReference.get();
    if (codeTable == null) {
      final List<HorizontalCoordinateSystem> coordinateSystems = EpsgCoordinateSystems
        .getHorizontalCoordinateSystems();
      codeTable = new EpsgCoordinateSystemsCodeTable("Horizontal Coordinate Systems",
        coordinateSystems);
      horizontalReference = new WeakReference<>(codeTable);
    }
    return codeTable;
  }

  public static EpsgCoordinateSystemsCodeTable vertical() {
    EpsgCoordinateSystemsCodeTable codeTable = verticalReference.get();
    if (codeTable == null) {
      final List<VerticalCoordinateSystem> coordinateSystems = EpsgCoordinateSystems
        .getVerticalCoordinateSystems();
      codeTable = new EpsgCoordinateSystemsCodeTable("Vertical Coordinate Systems",
        coordinateSystems);
      verticalReference = new WeakReference<>(codeTable);
    }
    return codeTable;
  }

  private Map<Identifier, List<Object>> codes = new HashMap<>();

  private final List<Identifier> identifiers;

  private String name;

  private EpsgCoordinateSystemsCodeTable(final String name,
    final List<? extends CoordinateSystem> coordinateSystems) {
    final List<Identifier> identifiers = new ArrayList<>();
    final Map<Identifier, List<Object>> codesById = this.codes;
    for (final CoordinateSystem coordinateSystem : coordinateSystems) {
      final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
      final Identifier id = Identifier.newIdentifier(coordinateSystemId);
      identifiers.add(id);
      final List<Object> code = Collections.singletonList(coordinateSystem);
      codesById.put(id, code);
    }
    this.identifiers = Collections.unmodifiableList(identifiers);
    this.codes = codesById;
  }

  @Override
  public Map<Identifier, List<Object>> getCodes() {
    return this.codes;
  }

  @Override
  public Identifier getIdentifier(final Object... values) {
    if (values.length == 1) {
      final Object value = values[0];
      final Identifier id = Identifier.newIdentifier(value);
      if (this.codes.containsKey(id)) {
        return id;
      } else {
        return CodeTable.super.getIdentifier(values);
      }
    } else {
      return CodeTable.super.getIdentifier(values);
    }
  }

  @Override
  public List<Identifier> getIdentifiers() {
    return this.identifiers;
  }

  @Override
  public String getIdFieldName() {
    return "coordinateSystemId";
  }

  @Override
  public String getName() {
    return this.name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <V> V getValue(final Identifier id) {
    final List<Object> code = this.codes.get(id);
    if (code == null) {
      return null;
    } else {
      return (V)code.get(0);
    }
  }

  @Override
  public List<Object> getValues(final Identifier id) {
    return this.codes.get(id);
  }

  @Override
  public String toString() {
    return this.name;
  }
}
