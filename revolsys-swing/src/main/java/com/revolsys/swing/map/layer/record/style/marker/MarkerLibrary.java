package com.revolsys.swing.map.layer.record.style.marker;

import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jeometry.common.logging.Logs;

import com.revolsys.io.map.MapObjectFactory;
import com.revolsys.io.map.MapObjectFactoryRegistry;
import com.revolsys.record.io.format.json.JsonObject;
import com.revolsys.util.Property;

public class MarkerLibrary extends MarkerGroup {
  private static final Map<String, MarkerLibrary> LIBRARIES = new LinkedHashMap<>();

  private static boolean initialized = false;

  public static void addLibrary(final MarkerLibrary library) {
    final String name = library.getName();
    final Map<String, MarkerLibrary> symbolLibraries = getLibraries();
    synchronized (symbolLibraries) {
      symbolLibraries.put(name, library);
    }
  }

  public static void factoryInit() {
    MapObjectFactoryRegistry.newFactory("markerLibrary", config -> {
      return new MarkerLibrary(config);
    });
    MapObjectFactoryRegistry.newFactory("markerGroup", config -> {
      return new MarkerGroup(config);
    });
    MapObjectFactoryRegistry.newFactory("markerSvg", config -> {
      return new SvgMarker(config);
    });
  }

  public static Marker findMarker(final String name) {
    final Map<String, MarkerLibrary> symbolLibraries = getLibraries();
    for (final MarkerLibrary library : symbolLibraries.values()) {
      final Marker symbol = library.getSymbol(name);
      if (symbol != null) {
        return symbol;
      }
    }
    return null;
  }

  public static List<Marker> getAllMarkers() {
    final List<Marker> markers = new ArrayList<>();
    for (final Marker marker : getAllSymbols()) {
      try {
        markers.add(marker);
      } catch (final Throwable e) {
        Logs.debug(MarkerLibrary.class, "Marker not found", e);
      }
    }
    return markers;
  }

  public static List<Marker> getAllSymbols() {
    final List<Marker> symbols = new ArrayList<>();
    final Map<String, MarkerLibrary> libraries = getLibraries();
    for (final MarkerLibrary library : libraries.values()) {
      library.addSymbolsToList(symbols);
    }
    return symbols;
  }

  public static Map<String, MarkerLibrary> getLibraries() {
    initialize();
    return LIBRARIES;
  }

  public static MarkerLibrary getLibrary(final String name) {
    final Map<String, MarkerLibrary> symbolLibraries = getLibraries();
    return symbolLibraries.get(name);
  }

  private static void initialize() {
    synchronized (LIBRARIES) {
      if (!initialized) {
        initialized = true;
        // ShapeMarker.init();
        GeometryMarker.init();
        try {
          final ClassLoader classLoader = MarkerLibrary.class.getClassLoader();
          final String resourceName = "META-INF/" + MarkerLibrary.class.getName() + ".json";
          final Enumeration<URL> resources = classLoader.getResources(resourceName);
          while (resources.hasMoreElements()) {
            final URL url = resources.nextElement();
            try {
              final MarkerLibrary library = MapObjectFactory.toObject(url);
              addLibrary(library);
            } catch (final Throwable e) {
              Logs.error(MarkerLibrary.class, "Unable to read resource" + url, e);
            }
          }
        } catch (final Throwable e) {
          Logs.error(MarkerLibrary.class, "Unable to read resources", e);
        }
      }
    }
  }

  public static MarkerLibrary newLibrary(final String name, final String title) {
    final Map<String, MarkerLibrary> libraries = getLibraries();
    synchronized (libraries) {
      MarkerLibrary library = libraries.get(name);
      if (library == null) {
        library = new MarkerLibrary(name, title);
        addLibrary(library);
      }
      return library;
    }
  }

  public static Marker newMarker(final String markerType) {
    if (Property.hasValue(markerType)) {
      final Marker symbol = findMarker(markerType);
      return symbol;
    }
    return null;
  }

  public MarkerLibrary() {
  }

  public MarkerLibrary(final Map<String, ? extends Object> properties) {
    setProperties(properties);
  }

  public MarkerLibrary(final String name, final String title) {
    super(name, title);
  }

  @Override
  public MarkerLibrary getMarkerLibrary() {
    return this;
  }

  @Override
  public String getTypeName() {
    return "markerLibrary";
  }

  @Override
  public JsonObject toMap() {
    final JsonObject map = super.toMap();
    return map;
  }
}
