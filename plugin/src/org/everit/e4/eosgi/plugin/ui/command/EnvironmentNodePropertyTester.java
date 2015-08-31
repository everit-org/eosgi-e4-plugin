package org.everit.e4.eosgi.plugin.ui.command;

import org.eclipse.core.expressions.PropertyTester;
import org.everit.e4.eosgi.plugin.core.dist.DistStatus;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EnvironmentNode;

/**
 * Example {@link PropertyTester} implementation.
 */
public class EnvironmentNodePropertyTester extends PropertyTester {

  private static final String DIST_STATUS = "distStatus";

  @Override
  public boolean test(final Object receiver, final String property, final Object[] args,
      final Object expectedValue) {
    if (receiver == null || property == null || expectedValue == null) {
      return false;
    }

    EnvironmentNode node = null;
    if (receiver instanceof EnvironmentNode) {
      node = (EnvironmentNode) receiver;
    }

    if (node == null) {
      return false;
    }

    if (DIST_STATUS.equals(property)) {
      return testType(node, expectedValue);
    }

    return false;
  }

  private boolean testType(final EnvironmentNode node, final Object expectedValue) {
    DistStatus distStatus = null;
    if (expectedValue instanceof String) {
      distStatus = DistStatus.valueOf((String) expectedValue);
    } else {
      return false;
    }
    return distStatus == node.getDistStatus();
  }

}
