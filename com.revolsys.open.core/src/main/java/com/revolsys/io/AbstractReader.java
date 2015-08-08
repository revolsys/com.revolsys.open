package com.revolsys.io;

import java.util.function.Consumer;

import com.revolsys.properties.BaseObjectWithProperties;

/**
 * The AbstracteReader is an implementation of the {@link Reader} interface,
 * which provides implementations of {@link #read()} and {@link #forEach(Consumer)}
 * which use the {@link Reader#iterator()} method which must be implemented by
 * subclasses.
 *
 * @author Paul Austin
 * @param <T> The type of object being read.
 */
public abstract class AbstractReader<T> extends BaseObjectWithProperties implements Reader<T> {

}
