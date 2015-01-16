package com.revolsys.visitor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.revolsys.filter.Filter;

/**
 * A visitor implementation which adds all the visited items to a List.
 *
 * @author Paul Austin
 * @param <T> The type of item to visit.
 */
public class CreateListVisitor<T> extends BaseVisitor<T> {
  private final List<T> list = new ArrayList<T>();

  public CreateListVisitor() {
  }

  public CreateListVisitor(final Comparator<T> comparator) {
    super(comparator);
  }

  public CreateListVisitor(final Filter<T> filter) {
    super(filter);
  }

  public CreateListVisitor(final Filter<T> filter,
    final Comparator<T> comparator) {
    super(filter, comparator);
  }

  @Override
  public boolean doVisit(final T item) {
    this.list.add(item);
    return true;
  }

  public List<T> getList() {
    return this.list;
  }
}
