package com.revolsys.gis.parallel;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;

public class ConverterProcess extends AbstractInOutProcess<DataObject> {

  private Converter<DataObject, DataObject> converter;

  public ConverterProcess() {
  }

  public ConverterProcess(
    final Converter<DataObject, DataObject> converter) {
    this.converter = converter;
  }

  public Converter<DataObject, DataObject> getConverter() {
    return converter;
  }

  @Override
  protected void run(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    if (converter != null) {
      while (true) {
        final DataObject source = in.read();
        final DataObject target = converter.convert(source);
        out.write(target);
      }
    }
  }

  public void setConverter(
    final Converter<DataObject, DataObject> converter) {
    this.converter = converter;
  }

}
