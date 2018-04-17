package com.revolsys.elevation.gridded.img;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.Maps;

public class HfaDictionary {

  private final Map<String, List<HfaField>> STANDARD_TYPES = Maps
    .<String, List<HfaField>> buildHash()
    .add("Edsc_Table", Arrays.asList(//
      new HfaField(1, 'l', "numrows")//
    )) //
    .add("Edsc_Column", Arrays.asList(//
      new HfaField(1, 'l', "numrows"), //
      new HfaField(1, 'L', "columnDataPtr"), //
      new HfaField(1, 'e', "dataType", Arrays.asList("integer", "real", "complex", "string")), //
      new HfaField(1, 'l', "maxNumChars")//
    ))
    .add("Eprj_Size", Arrays.asList(//
      new HfaField(1, 'd', "width"), //
      new HfaField(1, 'd', "height")//
    ))
    .add("Eprj_Coordinate", Arrays.asList(//
      new HfaField(1, 'd', "x"), //
      new HfaField(1, 'd', "y") //
    )) //
    .add("Eprj_MapInfo", Arrays.asList(//
      new HfaField(0, 'p', 'c', "proName"), //
      HfaField.newPointer(1, '*', "Eprj_Coordinate", "upperLeftCenter"), //
      HfaField.newPointer(1, '*', "Eprj_Coordinate", "lowerRightCenter"), //
      HfaField.newPointer(1, '*', "Eprj_Size", "pixelSize"), //
      new HfaField(0, 'p', 'c', "units") //
    )) //
    //
    .add("Eimg_StatisticsParameters830", Arrays.asList(//
      HfaField.newPointer(0, 'p', "Emif_String", "LayerNames"), //
      new HfaField(1, '*', 'b', "ExcludedValues"), //
      HfaField.newObject(1, "Emif_String", "AOIname"), //
      new HfaField(1, 'l', "SkipFactorX"), //
      new HfaField(1, 'l', "SkipFactorY"), //
      HfaField.newPointer(1, '*', "Edsc_BinFunction", "BinFunction") //
    )) //
    .add("Esta_Statistics", Arrays.asList(//
      new HfaField(1, 'd', "minimum"), //
      new HfaField(1, 'd', "maximum"), //
      new HfaField(1, 'd', "mean"), //
      new HfaField(1, 'd', "median"), //
      new HfaField(1, 'd', "mode"), //
      new HfaField(1, 'd', "stddev") //
    )) //
    .add("Edsc_BinFunction", Arrays.asList(//
      new HfaField(1, 'l', "numBins"), //
      new HfaField(1, 'e', "binFunctionType", "direct", "linear", "logarithmic", "explicit"), //
      new HfaField(1, 'd', "minLimit"), //
      new HfaField(1, 'd', "maxLimit"), //
      new HfaField(1, '*', 'b', "binLimits") //
    )) //
    .add("Eimg_NonInitializedValue", Arrays.asList( //
      new HfaField(1, '*', 'b', "valueBD") //
    )) //
    // TODO
    // .add("Eprj_MapProjection842", Arrays.asList(//
    // new HfaField(1, x{1:x{0:pcstring,}Emif_String,type), //
    // new HfaField(1, x{0:pcstring,}Emif_String,MIFDictionary), //
    // new HfaField(0, true, 'c', "MIFObject,}Emif_MIFObject,projection"), //
    // new HfaField(1, x{0:pcstring,}Emif_String,title") //
    // )) //
    // //
    // .add("Emif_MIFObject", Arrays.asList(//
    // new HfaField(1, x{0:pcstring,}Emif_String,type"), //
    // new HfaField(1, x{0:pcstring,}Emif_String,MIFDictionary"), //
    // new HfaField(0, true, 'c', "MIFObject") //
    // )) //
    // //
    .add("Eprj_ProParameters", Arrays.asList(//
      new HfaField(1, 'e', "proType", "EPRJ_INTERNAL", "EPRJ_EXTERNAL"), //
      new HfaField(1, 'l', "proNumber"), //
      new HfaField(0, 'p', 'c', "proExeName"), //
      new HfaField(0, 'p', 'c', "proName"), //
      new HfaField(1, 'l', "proZone"), //
      new HfaField(0, 'p', 'd', "proParams"), //
      HfaField.newPointer(1, '*', "Eprj_Spheroid", "proSpheroid") //
    )) //
    //
    .add("Eprj_Datum", Arrays.asList(//
      new HfaField(0, 'p', 'c', "datumname"), //
      new HfaField(1, 'e', "type", "EPRJ_DATUM_PARAMETRIC", "EPRJ_DATUM_GRID",
        "EPRJ_DATUM_REGRESSION"), //
      new HfaField(0, 'p', 'd', "params"), //
      new HfaField(0, 'p', 'c', "gridname") //
    )) //
    //
    .add("Eprj_Spheroid", Arrays.asList(//
      new HfaField(0, 'p', "csphereName"), //
      new HfaField(1, 'd', "a"), //
      new HfaField(1, 'd', "b"), //
      new HfaField(1, 'd', "eSquared"), //
      new HfaField(1, 'd', "radius") //
    )) //
    .getMap();

  private final List<HfaType> types;

  public HfaDictionary(final List<HfaType> types) {
    this.types = types;
    for (final HfaType type : types) {
      type.completeDefn(this);
    }
  }

  HfaType findType(final String name) {
    for (final HfaType type : this.types) {
      if (type.equalsTypeName(name)) {
        return type;
      }
    }

    final List<HfaField> fields = this.STANDARD_TYPES.get(name);
    if (fields != null) {
      final HfaType newType = new HfaType(name, fields);
      if (newType.completeDefn(this)) {
        this.types.add(newType);
      }
    }

    return null;
  }

  int getItemSize(final char type) {
    switch (type) {
      case '1':
      case '2':
      case '4':
      case 'c':
      case 'C':
        return 1;

      case 'e':
      case 's':
      case 'S':
        return 2;

      case 't':
      case 'l':
      case 'L':
      case 'f':
        return 4;

      case 'd':
      case 'm':
        return 8;

      case 'M':
        return 16;

      case 'b':
        return -1;

      case 'o':
      case 'x':
        return 0;

      default:
        return 0;
    }
  }
}
