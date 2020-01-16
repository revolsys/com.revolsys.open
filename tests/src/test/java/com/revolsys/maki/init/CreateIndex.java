package com.revolsys.maki.init;

import java.io.File;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.swing.map.layer.record.style.marker.MarkerLibrary;
import com.revolsys.swing.map.layer.record.style.marker.SvgMarker;

public class CreateIndex {
  public static void main(final String[] args) {
    MarkerLibrary.findMarker("maki/police");
    final File makiFile = new File("../maki/_includes/maki.json");
    final List<JsonObject> symbolList = Json.toMapList(makiFile);

    final MarkerLibrary library = new MarkerLibrary("maki", "Maki");

    for (final MapEx symbolMap : symbolList) {
      final String name = "maki/" + symbolMap.getString("icon");
      final String title = symbolMap.getString("name");
      final SvgMarker symbol = new SvgMarker(name, title);
      library.addSymbol(symbol);
    }
    final File libraryFile = new File(
      "../com.revolsys.open.maki/src/main/resources/META-INF/com.revolsys.swing.map.symbol.MarkerLibrary.json");
    library.writeToFile(libraryFile);

    final MarkerLibrary library2 = MapObjectFactory.toObject(libraryFile);
    System.out.println(library2);
  }
}
