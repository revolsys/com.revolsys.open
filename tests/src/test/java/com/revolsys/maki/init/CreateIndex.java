package com.revolsys.maki.init;

import java.io.File;
import java.util.List;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.swing.map.symbol.SymbolLibrary;

public class CreateIndex {
  public static void main(final String[] args) {
    SymbolLibrary.findSymbol("maki/police");
    final File makiFile = new File("../maki/_includes/maki.json");
    final List<MapEx> symbolList = Json.toMapList(makiFile);

    final SymbolLibrary symbolLibrary = new SymbolLibrary("maki", "Maki");

    for (final MapEx symbolMap : symbolList) {
      final String name = "maki/" + symbolMap.getString("icon");
      final String title = symbolMap.getString("name");
      symbolLibrary.addSymbolSvg(name, title);
    }
    final File symbolLibraryFile = new File(
      "src/main/resources/META-INF/com.revolsys.core.test.swing.map.symbol.SymbolLibrary.json");
    MapObjectFactory.write(symbolLibraryFile, symbolLibrary);

    final SymbolLibrary symbolLibrary2 = MapObjectFactory.toObject(symbolLibraryFile);
    System.out.println(symbolLibrary2);
  }
}
