/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.osgi.dev.e4.plugin.util;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

import org.apache.commons.collections.list.TreeList;

/**
 * Helper class to flatten a DAG to an ordered list. Every element that is over another one in the
 * DAG will be in front of the other in the result list.
 */
public final class DAGFlattener<T> {

  private final Function<T, List<T>> childResolver;

  private final Comparator<T> comparator;

  public DAGFlattener(final Comparator<T> comparator,
      final Function<T, List<T>> childResolver) {
    this.comparator = comparator;
    this.childResolver = childResolver;
  }

  public List<T> flatten(final T root) {
    Set<T> visited = new TreeSet<>(comparator);

    @SuppressWarnings("unchecked")
    List<T> result = new TreeList();

    ListIterator<T> resultIterator = result.listIterator();
    flattenRecurse(root, resultIterator, visited);
    return result;
  }

  private void flattenRecurse(final T node, final ListIterator<T> resultIterator,
      final Set<T> visited) {

    resultIterator.add(node);
    visited.add(node);

    List<T> children = childResolver.apply(node);
    if (children.isEmpty()) {
      return;
    }

    ListIterator<T> iterator = children.listIterator(children.size());
    while (iterator.hasPrevious()) {
      T childNode = iterator.previous();
      if (!visited.contains(childNode)) {
        flattenRecurse(childNode, resultIterator, visited);
        iterateBackToBeAfterNode(resultIterator, node);
      }
    }
  }

  private void iterateBackToBeAfterNode(final ListIterator<T> resultIterator, final T node) {
    T currentNode = resultIterator.previous();
    while (comparator.compare(node, currentNode) != 0) {
      currentNode = resultIterator.previous();
    }
    resultIterator.next();
  }
}
