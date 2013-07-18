package com.revolsys.io.map;

import java.util.Collection;
import java.util.Map;

public interface MapSerializer {
  /**
   * <p>Convert the object to a Map of property name, value pairs. The values can be one of
   * the following supported types. Other values should be converted to one of these values.</p>
   * 
   * <ul>
   *   <li>boolean or {@link Boolean}</li>
   *   <li>byte or {@link Byte}</li>
   *   <li>short or {@link Short}</li>
   *   <li>int or {@link Integer}</li>
   *   <li>long or {@link Long}</li>
   *   <li>float or {@link Float}</li>
   *   <li>double or {@link Double}</li>
   *   <li>{@link String}</li>
   *   <li>{@link Number} subclasses</li>
   *   <li>{@link Collection} of supported values</li>
   *   <li>{@link Map}<String,Object> of supported values</li>
   *   <li>null</li>
   * </ul>
   * @return
   */
  Map<String, Object> toMap();
}
