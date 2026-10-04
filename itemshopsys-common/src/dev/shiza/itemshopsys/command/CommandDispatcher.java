package dev.shiza.itemshopsys.command;

public interface CommandDispatcher {

  void dispatchCommand(final String command);

  /**
   * Dispatches a command that is related to the given player (may be null). Platforms with
   * region-based threading (Folia) can use it to run the command on the proper thread.
   */
  default void dispatchCommand(final String command, final String playerName) {
    dispatchCommand(command);
  }
}
