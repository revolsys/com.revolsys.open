package com.revolsys.maki;

import java.io.File;
import java.util.List;
import java.util.Map;

import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.record.io.format.json.Json;
import com.revolsys.swing.map.symbol.SymbolLibrary;

public class CreateIndex {
  public static void main(final String[] args) {
    SymbolLibrary.findSymbol("maki/police");
    final File makiFile = new File("../maki/_includes/maki.json");
    final List<Map<String, Object>> map = Json.toMapList(makiFile);

    final SymbolLibrary symbolLibrary = new SymbolLibrary("maki", "Maki");

    for (final Map<String, Object> symbolMap : map) {
      final String name = "maki/" + (String)symbolMap.get("icon");
      final String title = (String)symbolMap.get("name");
      symbolLibrary.addSymbolSvg(name, title);
    }
    final File symbolLibraryFile = new File(
      "src/main/resources/META-INF/com.revolsys.swing.map.symbol.SymbolLibrary.json");
    MapObjectFactory.write(symbolLibraryFile, symbolLibrary);

    final SymbolLibrary symbolLibrary2 = MapObjectFactory.toObject(symbolLibraryFile);
    System.out.println(symbolLibrary2);
  }
}
