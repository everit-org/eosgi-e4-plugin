package org.everit.e4.eosgi.plugin.ui.command;

import org.eclipse.core.expressions.PropertyTester;

/**
 * Example {@link PropertyTester} implementation.
 */
public class ExamplePropertyTester extends PropertyTester {

  @Override
  public boolean test(final Object receiver, final String property, final Object[] args,
      final Object expectedValue) {
    return true;
  }

}
