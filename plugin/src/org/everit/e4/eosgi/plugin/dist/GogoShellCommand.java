package org.everit.e4.eosgi.plugin.dist;

/**
 * Enum fo Gogo shell commands.
 */
public enum GogoShellCommand {
  CLOSE("close"), EXIT("exit"), SHUTDOWN("shutdown"), SS("ss");

  private String command;

  GogoShellCommand(final String command) {
    this.command = command;
  }

  public String getCommand() {
    return command;
  }
}
