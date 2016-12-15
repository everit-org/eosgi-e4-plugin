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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.collections.list.TreeList;

/**
 * Helper class to flatten a DAG to an ordered list. Every element that is over another one in the
 * DAG will be in front of the other in the result list.
 * 
 * @param <K>
 *          The key of the nodes.
 * @param <T>
 *          The type of the nodes.
 */
public final class DAGFlattener<K, T> {

  public static final class KeyWithNodes<K, N> {
    public final K key;

    public final Set<N> nodes = new HashSet<>();

    public KeyWithNodes(final K key) {
      this.key = key;
    }

  }

  private final Function<T, List<T>> childResolver;

  private final Function<T, K> keyGenerator;

  public DAGFlattener(final Function<T, K> keyGenerator,
      final Function<T, List<T>> childResolver) {
    this.keyGenerator = keyGenerator;
    this.childResolver = childResolver;
  }

  public List<KeyWithNodes<K, T>> flatten(final T root) {
    Map<K, KeyWithNodes<K, T>> visited = new HashMap<>();

    @SuppressWarnings("unchecked")
    List<KeyWithNodes<K, T>> result = new TreeList();

    ListIterator<KeyWithNodes<K, T>> resultIterator = result.listIterator();
    flattenRecurse(keyGenerator.apply(root), root, resultIterator, visited);
    return result;
  }

  private void flattenRecurse(final K nodeKey, final T node,
      final ListIterator<KeyWithNodes<K, T>> resultIterator,
      final Map<K, KeyWithNodes<K, T>> visited) {

    KeyWithNodes<K, T> keyWithNodes = visited.get(nodeKey);
    if (keyWithNodes == null) {
      keyWithNodes = new KeyWithNodes<>(nodeKey);
      visited.put(nodeKey, keyWithNodes);
    }

    keyWithNodes.nodes.add(node);
    resultIterator.add(keyWithNodes);

    List<T> children = childResolver.apply(node);
    if (children.isEmpty()) {
      return;
    }

    ListIterator<T> iterator = children.listIterator(children.size());
    while (iterator.hasPrevious()) {
      T childNode = iterator.previous();
      K childNodeKey = keyGenerator.apply(childNode);
      if (!visited.containsKey(childNodeKey)) {
        flattenRecurse(childNodeKey, childNode, resultIterator, visited);
        iterateBackToBeAfterNode(resultIterator, nodeKey);
      }
    }
  }

  private void iterateBackToBeAfterNode(final ListIterator<KeyWithNodes<K, T>> resultIterator,
      final K nodeKey) {
    KeyWithNodes<K, T> currentKeyWithNodes = resultIterator.previous();
    while (currentKeyWithNodes != null && !currentKeyWithNodes.key.equals(nodeKey)) {
      currentKeyWithNodes = resultIterator.previous();
    }
    resultIterator.next();
  }
}
