package com.revolsys.swing.map.symbol;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.revolsys.collection.map.MapEx;
import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.logging.Logs;
import com.revolsys.spring.resource.Resource;
import com.revolsys.swing.map.layer.record.style.marker.Marker;
import com.revolsys.swing.map.layer.record.style.marker.ShapeMarker;
import com.revolsys.util.Property;

public class SymbolLibrary extends SymbolGroup {
  private static final Map<String, SymbolLibrary> SYMBOL_LIBRARIES = new LinkedHashMap<>();

  private static boolean initialized;

  public static void addSymbolLibrary(final SymbolLibrary symbolLibrary) {
    final String name = symbolLibrary.getName();
    final Map<String, SymbolLibrary> symbolLibraries = getSymbolLibraries();
    synchronized (symbolLibraries) {
      symbolLibraries.put(name, symbolLibrary);
    }
  }

  public static void factoryInit() {
    MapObjectFactoryRegistry.newFactory("symbolLibrary", (config) -> {
      return new SymbolLibrary(config);
    });
    MapObjectFactoryRegistry.newFactory("symbolGroup", (config) -> {
      return new SymbolGroup(config);
    });
    MapObjectFactoryRegistry.newFactory("symbolShape", (config) -> {
      return new ShapeSymbol(config);
    });
    MapObjectFactoryRegistry.newFactory("symbolSvg", (config) -> {
      return new SvgSymbol(config);
    });
  }

  public static Symbol findSymbol(final String name) {
    final Map<String, SymbolLibrary> symbolLibraries = getSymbolLibraries();
    for (final SymbolLibrary symbolLibrary : symbolLibraries.values()) {
      final Symbol symbol = symbolLibrary.getSymbol(name);
      if (symbol != null) {
        return symbol;
      }
    }
    return null;
  }

  public static List<Marker> getAllMarkers() {
    final List<Marker> markers = new ArrayList<>();
    for (final Symbol symbol : getAllSymbols()) {
      final Marker marker = symbol.newMarker();
      markers.add(marker);
    }
    return markers;
  }

  public static List<Symbol> getAllSymbols() {
    final List<Symbol> symbols = new ArrayList<>();
    final Map<String, SymbolLibrary> symbolLibraries = getSymbolLibraries();
    for (final SymbolLibrary symbolLibrary : symbolLibraries.values()) {
      symbolLibrary.addSymbolsToList(symbols);
    }
    return symbols;
  }

  public static Map<String, SymbolLibrary> getSymbolLibraries() {
    initialize();
    return SYMBOL_LIBRARIES;
  }

  public static SymbolLibrary getSymbolLibrary(final String name) {
    initialize();
    return SYMBOL_LIBRARIES.get(name);
  }

  private static void initialize() {
    synchronized (SYMBOL_LIBRARIES) {
      if (!initialized) {
        initialized = true;
        ShapeMarker.init();
        try {
          final ClassLoader classLoader = MapObjectFactoryRegistry.class.getClassLoader();
          final String resourceName = "META-INF/" + SymbolLibrary.class.getName() + ".json";
          final Enumeration<URL> resources = classLoader.getResources(resourceName);
          while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try {
              final Resource resource = Resource.getResource(url);
              final SymbolLibrary symbolLibrary = MapObjectFactory.toObject(resource);
              addSymbolLibrary(symbolLibrary);
            } catch (final Throwable e) {
              Logs.error(MapObjectFactoryRegistry.class, "Unable to read resource" + url, e);
            }
          }
        } catch (final Throwable e) {
          Logs.error(MapObjectFactoryRegistry.class, "Unable to read resources", e);
        }
      }
    }
  }

  public static Marker newMarker(final String markerType) {
    if (Property.hasValue(markerType)) {
      final Symbol symbol = findSymbol(markerType);
      if (symbol != null) {
        return symbol.newMarker();
      }
    }
    return null;
  }

  public static SymbolLibrary newSymbolLibrary(final String name, final String title) {
    synchronized (SYMBOL_LIBRARIES) {
      SymbolLibrary symbolLibrary = SYMBOL_LIBRARIES.get(name);
      if (symbolLibrary == null) {
        symbolLibrary = new SymbolLibrary(name, title);
        addSymbolLibrary(symbolLibrary);
      }
      return symbolLibrary;
    }
  }

  public SymbolLibrary() {
  }

  public SymbolLibrary(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public SymbolLibrary(final String name, final String title) {
    super(name, title);
  }

  @Override
  public SymbolLibrary getSymbolLibrary() {
    return this;
  }

  @Override
  public String getTypeName() {
    return "symbolLibrary";
  }

  @Override
  public MapEx toMap() {
    final MapEx map = super.toMap();
    return map;
  }
}
