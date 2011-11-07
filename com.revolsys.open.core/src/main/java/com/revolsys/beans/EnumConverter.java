package com.revolsys.beans;

import org.apache.commons.beanutils.converters.AbstractConverter;

public final class EnumConverter extends AbstractConverter {
   public EnumConverter() {
        super();
    }

    public EnumConverter(Object defaultValue) {
        super(defaultValue);
    }

    protected Class getDefaultType() {
        return Enum.class;
    }

   protected Object convertToType(Class type, Object value) throws Throwable {
     if (type.isEnum()) {
       return Enum.valueOf(type, value.toString());
     } else {
        return value;
     }
    }


}
