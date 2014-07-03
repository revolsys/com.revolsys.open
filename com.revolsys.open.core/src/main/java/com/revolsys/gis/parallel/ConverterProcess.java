package com.revolsys.gis.parallel;

import org.springframework.core.convert.converter.Converter;

import com.revolsys.data.record.Record;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.BaseInOutProcess;

public class ConverterProcess extends BaseInOutProcess<Record, Record> {

  private Converter<Record, Record> converter;

  public ConverterProcess() {
  }

  public ConverterProcess(final Converter<Record, Record> converter) {
    this.converter = converter;
  }

  public Converter<Record, Record> getConverter() {
    return converter;
  }

  @Override
  protected void process(final Channel<Record> in,
    final Channel<Record> out, final Record object) {
    if (converter != null) {
      final Record target = converter.convert(object);
      out.write(target);
    }
  }

  public void setConverter(final Converter<Record, Record> converter) {
    this.converter = converter;
  }

}
