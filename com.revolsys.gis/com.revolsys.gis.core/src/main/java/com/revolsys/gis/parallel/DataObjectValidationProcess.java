package com.revolsys.gis.parallel;

import java.util.Map;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.types.DataType;
import com.revolsys.gis.model.data.validator.AttributeValueValidator;
import com.revolsys.gis.model.data.validator.DataObjectValidator;
import com.revolsys.parallel.channel.Channel;
import com.revolsys.parallel.process.AbstractInOutProcess;

public class DataObjectValidationProcess extends
  AbstractInOutProcess<DataObject> {
  private final DataObjectValidator validator = new DataObjectValidator();

  @Override
  protected void run(
    final Channel<DataObject> in,
    final Channel<DataObject> out) {
    while (true) {
      final DataObject object = in.read();
      validator.isValid(object);
      out.write(object);
    }
  }

  public void setValidators(
    final Map<DataType, AttributeValueValidator> validators) {
    validator.addValidators(validators);
  }
}
