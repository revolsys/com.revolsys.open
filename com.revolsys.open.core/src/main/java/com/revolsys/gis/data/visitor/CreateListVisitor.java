package com.revolsys.gis.data.visitor;

import java.util.ArrayList;
import java.util.List;

import com.revolsys.collection.Visitor;

/**
 * A visitor implementation which adds all the visited items to a List.
 * 
 * @author Paul Austin
 * @param <T> The type of item to visit.
 */
public class CreateListVisitor<T> implements Visitor<T> {
  private final List<T> list = new ArrayList<T>();

  public List<T> getList() {
    return list;
  }

  public boolean visit(final T item) {
    list.add(item);
    return true;
  }
}
