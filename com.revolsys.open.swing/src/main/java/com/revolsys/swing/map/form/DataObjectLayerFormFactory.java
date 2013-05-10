package com.revolsys.swing.map.form;

import javax.swing.JComponent;

import org.apache.commons.beanutils.ConstructorUtils;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.swing.map.layer.dataobject.DataObjectLayer;

public class DataObjectLayerFormFactory {
  public static final String FORM_CLASS_NAME = "formClassName";

  public static JComponent createFormComponent(final DataObjectLayer layer,
    final DataObject object) {
    final String formClassName = layer.getProperty(FORM_CLASS_NAME);
    DataObjectLayerForm form = null;
    if (StringUtils.hasText(formClassName)) {
      try {
        final Class<?> formClass = Class.forName(formClassName);
        final Object[] args = {
          layer
        };
        form = (DataObjectLayerForm)ConstructorUtils.invokeConstructor(
          formClass, args);
      } catch (final Throwable e) {
        LoggerFactory.getLogger(DataObjectLayerFormFactory.class).error(
          "Unable to create form " + formClassName, e);
      }
    }
    if (form == null) {
      form = new DataObjectLayerForm(layer);
    }
    form.setObject(object);
    return form;
  }
}
