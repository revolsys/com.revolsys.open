package com.revolsys.ui.html.serializer;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.builder.HtmlUiBuilder;
import com.revolsys.ui.html.serializer.key.AbstractKeySerializer;
import com.revolsys.ui.html.serializer.type.TypeSerializer;

/**
 * Serialize a value using a method on the {@link HtmlUiBuilder}.
 *
 * @author Paul Austin
 */
public class BuilderMethodSerializer extends AbstractKeySerializer implements TypeSerializer {
  /** The HTML UI Builder */
  private HtmlUiBuilder<?> builder;

  /** The method on the builder */
  private Method method;

  public BuilderMethodSerializer() {
  }

  /**
   * Construt a new HtmlUiBuilderMethodSerializer.
   *
   * @param builder The HTML UI Builder the method is on.
   * @param method The serializer method.
   */
  public BuilderMethodSerializer(final String name, final HtmlUiBuilder<?> builder,
    final Method method) {
    super(name);
    this.builder = builder;
    this.method = method;
  }

  /**
   * Serialize the value to the XML writer.
   *
   * @param out The XML writer to serialize to.
   * @param value The object to get the value from.
   * @throws IOException If there was an I/O error serializing the value.
   */
  @Override
  public void serialize(final XmlWriter out, final Object value) {
    try {
      this.method.invoke(this.builder, out, value);
    } catch (final IllegalAccessException e) {
      throw new RuntimeException(e.getMessage(), e);
    } catch (final InvocationTargetException e) {
      final Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) {
        throw (RuntimeException)cause;
      } else if (cause instanceof Error) {
        throw (Error)cause;
      } else {
        throw new RuntimeException(cause.getMessage(), cause);
      }
    }
  }
}
