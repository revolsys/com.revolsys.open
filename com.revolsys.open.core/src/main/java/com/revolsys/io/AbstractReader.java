package com.revolsys.io;

import com.revolsys.collection.Visitor;
import com.revolsys.properties.BaseObjectWithProperties;

/**
 * The AbstracteReader is an implementation of the {@link Reader} interface,
 * which provides implementations of {@link #read()} and {@link #visit(Visitor)}
 * which use the {@link Reader#iterator()} method which must be implemented by
 * subclasses.
 *
 * @author Paul Austin
 * @param <T> The type of object being read.
 */
public abstract class AbstractReader<T> extends BaseObjectWithProperties implements Reader<T> {

}
