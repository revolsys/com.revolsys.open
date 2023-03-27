package com.revolsys.record.code;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Consumer;

import org.jeometry.common.data.identifier.Identifier;
import org.jeometry.common.data.type.DataTypes;
import org.jeometry.coordinatesystem.model.CoordinateSystem;
import org.jeometry.coordinatesystem.model.HorizontalCoordinateSystem;
import org.jeometry.coordinatesystem.model.VerticalCoordinateSystem;
import org.jeometry.coordinatesystem.model.systems.EpsgCoordinateSystems;

public class EpsgCoordinateSystemsCodeTable extends AbstractCodeTable {

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

  private String name;

  private EpsgCoordinateSystemsCodeTable(final String name,
    final List<? extends CoordinateSystem> coordinateSystems) {
    for (final CoordinateSystem coordinateSystem : coordinateSystems) {
      final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
      final Identifier id = Identifier.newIdentifier(coordinateSystemId);
      addEntry(id, coordinateSystem);
    }
  }

  @Override
  public void close() {
  }

  @Override
  public Identifier getIdentifier(Consumer<CodeTableEntry> callback, Object value) {
    CoordinateSystem coordinateSystem = null;
    if (value instanceof CoordinateSystem) {
      coordinateSystem = (CoordinateSystem)value;
    } else {
      try {
        final Integer intValue = DataTypes.INT.toObject(value);
        final Identifier id = Identifier.newIdentifier(intValue);
        if (hasIdentifier(id)) {
          return id;
        }
      } catch (final Exception e) {
        coordinateSystem = EpsgCoordinateSystems.getCoordinateSystem(value.toString());
      }
    }
    if (coordinateSystem != null) {
      final int coordinateSystemId = coordinateSystem.getCoordinateSystemId();
      final Identifier id = Identifier.newIdentifier(coordinateSystemId);
      if (hasIdentifier(id)) {
        return id;
      }
    }
    return null;
  }

  @Override
  public String getIdFieldName() {
    return "coordinateSystemId";
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public int getValueFieldLength() {
    return 80;
  }

  @Override
  public String toString() {
    return this.name;
  }

}
