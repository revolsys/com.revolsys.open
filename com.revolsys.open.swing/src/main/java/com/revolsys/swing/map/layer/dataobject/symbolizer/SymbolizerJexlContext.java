package com.revolsys.swing.map.layer.dataobject.symbolizer;

import java.util.Map;

import org.apache.commons.jexl.JexlContext;

import com.revolsys.gis.data.model.DataObject;
import com.revolsys.gis.data.model.DataObjectJexlContext;

public class SymbolizerJexlContext implements JexlContext {

  private static final ThreadLocal<DataObjectJexlContext> CONTEXT = new ThreadLocal<DataObjectJexlContext>();

  private static final JexlContext INSTANCE = new SymbolizerJexlContext();

  public static JexlContext getInstance() {
    return INSTANCE;
  }

  public static DataObjectJexlContext getJexlContext() {
    DataObjectJexlContext context = CONTEXT.get();
    if (context == null) {
      context = new DataObjectJexlContext();
      CONTEXT.set(context);
    }
    return context;
  }

  public static void setDataObject(final DataObject dataObject) {
    final DataObjectJexlContext context = getJexlContext();
    context.setObject(dataObject);
  }

  private SymbolizerJexlContext() {
  }

  @Override
  public Map getVars() {
    return getJexlContext().getVars();
  }

  @Override
  public void setVars(final Map values) {
  }

}
