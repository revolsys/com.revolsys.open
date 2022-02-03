package com.revolsys.odata.service.processor;

import java.io.OutputStream;

import org.apache.olingo.server.api.ODataContent;
import org.apache.olingo.server.api.serializer.SerializerStreamResult;

import com.revolsys.io.BaseCloseable;
import com.revolsys.odata.model.ODataEntityIterator;

public class ODataEntityInteratorDataContent implements ODataContent {
  private final ODataContent dataContent;

  private final ODataEntityIterator iterator;

  public ODataEntityInteratorDataContent(final SerializerStreamResult serializerResult,
    final ODataEntityIterator iterator) {
    super();
    this.dataContent = serializerResult.getODataContent();
    this.iterator = iterator;
  }

  @Override
  public void write(final OutputStream stream) {
    try (
      BaseCloseable c = this.iterator) {
      this.dataContent.write(stream);
    }
  }

}
