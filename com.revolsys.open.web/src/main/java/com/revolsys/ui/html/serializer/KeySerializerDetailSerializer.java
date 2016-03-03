/*
 * Copyright 2004-2005 Revolution Systems Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.revolsys.ui.html.serializer;

import java.util.Collections;
import java.util.List;

import com.revolsys.record.io.format.xml.XmlWriter;
import com.revolsys.ui.html.serializer.key.KeySerializer;

public class KeySerializerDetailSerializer implements LabelValueListSerializer {
  private Object object;

  private final List<KeySerializer> serializers;

  public KeySerializerDetailSerializer(final List<KeySerializer> serializers) {
    if (serializers == null) {
      this.serializers = Collections.emptyList();
    } else {
      this.serializers = serializers;
    }
  }

  public Object getObject() {
    return this.object;
  }

  @Override
  public int getSize() {
    return this.serializers.size();
  }

  @Override
  public String getValueCss(final int index) {
    final KeySerializer serializer = this.serializers.get(index);
    return serializer.getName();
  }

  @Override
  public void serializeLabel(final XmlWriter out, final int index) {
    if (index < getSize()) {
      final KeySerializer serializer = this.serializers.get(index);
      final String label = serializer.getLabel();
      out.text(label);
    } else {
      out.entityRef("nbsp");
    }
  }

  @Override
  public void serializeValue(final XmlWriter out, final int index) {
    if (index < getSize()) {
      final KeySerializer serializer = this.serializers.get(index);
      serializer.serialize(out, this.object);
    } else {
      out.entityRef("nbsp");
    }
  }

  public void setObject(final Object object) {
    this.object = object;
  }
}
