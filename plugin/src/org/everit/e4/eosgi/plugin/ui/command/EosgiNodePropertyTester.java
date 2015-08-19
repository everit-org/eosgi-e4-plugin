package org.everit.e4.eosgi.plugin.ui.command;

import org.eclipse.core.expressions.PropertyTester;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNode;
import org.everit.e4.eosgi.plugin.ui.navigator.nodes.EosgiNodeType;

/**
 * Example {@link PropertyTester} implementation.
 */
public class EosgiNodePropertyTester extends PropertyTester {

  private static final String TYPE = "type";

  @Override
  public boolean test(final Object receiver, final String property, final Object[] args,
      final Object expectedValue) {
    if (receiver == null || property == null || expectedValue == null) {
      return false;
    }

    EosgiNode eosgiNode = null;
    if (receiver instanceof EosgiNode) {
      eosgiNode = (EosgiNode) receiver;
    }

    if (eosgiNode == null) {
      return false;
    }

    if (TYPE.equals(property)) {
      return testType(eosgiNode, expectedValue);
    }

    return false;
  }

  private boolean testType(final EosgiNode eosgiNode, final Object expectedValue) {
    EosgiNodeType eosgiNodeType = null;
    if (expectedValue instanceof String) {
      eosgiNodeType = EosgiNodeType.valueOf((String) expectedValue);
    } else {
      return false;
    }
    return eosgiNodeType == eosgiNode.getType();
  }

}
