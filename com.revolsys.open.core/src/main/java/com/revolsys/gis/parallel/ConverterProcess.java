package com.revolsys.gis.parallel;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class ConverterProcess extends BaseInOutProcess<DataObject, DataObject> {

  private Converter<DataObject, DataObject> converter;

  public ConverterProcess() {
  }

  public ConverterProcess(final Converter<DataObject, DataObject> converter) {
    this.converter = converter;
  }

  public Converter<DataObject, DataObject> getConverter() {
    return converter;
  }

  @Override
  protected void process(
    final Channel<DataObject> in,
    final Channel<DataObject> out,
    final DataObject object) {
    if (converter != null) {
      final DataObject target = converter.convert(object);
      out.write(target);
    }
  }

  public void setConverter(final Converter<DataObject, DataObject> converter) {
    this.converter = converter;
  }

}
