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
package org.everit.osgi.dev.e4.plugin.ui.command;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Dist status property tester implementation.
 */
// TODO Remove it if not necessary.
public class EnvironmentNodePropertyTester extends PropertyTester {

  // private static final String DIST_STATUS = "distStatus";

  @Override
  public boolean test(final Object receiver, final String property, final Object[] args,
      final Object expectedValue) {
    // if ((receiver == null) || (property == null) || (expectedValue == null)) {
    // return false;
    // }
    //
    // EnvironmentNode node = null;
    // if (receiver instanceof EnvironmentNode) {
    // node = (EnvironmentNode) receiver;
    // }
    //
    // if (node == null) {
    // return false;
    // }
    //
    // if (DIST_STATUS.equals(property)) {
    // return testType(node, expectedValue);
    // }

    return false;
  }

  // private boolean testType(final EnvironmentNode node, final Object expectedValue) {
    // DistStatus distStatus = null;
    // if (expectedValue instanceof String) {
    // distStatus = DistStatus.valueOf((String) expectedValue);
    // } else {
    // return false;
    // }
    // return distStatus == node.getDistStatus();
  //// return false;
  // }

}
