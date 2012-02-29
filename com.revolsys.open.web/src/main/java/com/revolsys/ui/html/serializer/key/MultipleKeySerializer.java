package com.revolsys.ui.html.serializer.key;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.io.xml.XmlWriter;

public class MultipleKeySerializer extends AbstractKeySerializer {
  private List<KeySerializer> serializers = new ArrayList<KeySerializer>();

  public MultipleKeySerializer() {
  }

  public MultipleKeySerializer(String name, String label) {
    super(name, label);
  }

  public MultipleKeySerializer(String name) {
    super(name);
  }

  public void serialize(XmlWriter out, Object object) {
    for (KeySerializer serializer : serializers) {
      serializer.serialize(out, object);
    }
  }

  public List<KeySerializer> getSerializers() {
    return serializers;
  }

  public void setSerializers(List<KeySerializer> serializers) {
    this.serializers = serializers;
  }
}
