package dev.shiza.itemshopsys.command;

import dev.shiza.itemshopsys.ItemShopLogger;
import dev.shiza.itemshopsys.client.ItemShopClient;
import dev.shiza.itemshopsys.client.ItemShopClientConfig;
import dev.shiza.itemshopsys.client.ItemShopServer;
import dev.shiza.itemshopsys.client.command.ExecutableCommand;
import dev.shiza.itemshopsys.player.PlayerAccessor;
import java.util.List;

public final class CommandPoolTask implements Runnable {

  private final ItemShopLogger logger;
  private final ItemShopClientConfig config;
  private final ItemShopClient client;
  private final PlayerAccessor playerAccessor;
  private final CommandDispatcher commandDispatcher;

  public CommandPoolTask(
      final ItemShopLogger logger,
      final ItemShopClientConfig config,
      final ItemShopClient client,
      final PlayerAccessor playerAccessor,
      final CommandDispatcher commandDispatcher) {
    this.logger = logger;
    this.config = config;
    this.client = client;
    this.playerAccessor = playerAccessor;
    this.commandDispatcher = commandDispatcher;
  }

  @Override
  public void run() {
    // resolved on every run, so "/itemshopsys reload" applies changes to the servers list
    for (final ItemShopServer server : config.resolveServers()) {
      try {
        processServer(server);
      } catch (final Exception exception) {
        logger.error(
            String.format(
                "Could not process server %s: %s", server.getServerId(), exception));
      }
    }
  }

  private void processServer(final ItemShopServer server) {
    final List<ExecutableCommand> executableCommands =
        client
            .retrieveCommands(config.shopId, server.getServerId(), server.getServerToken())
            .getCommands();
    if (executableCommands.isEmpty()) {
      logger.debug(
          String.format("No commands to execute were retrieved for %s.", server.getServerId()));
      return;
    }

    executableCommands.forEach(command -> processCommand(server, command));
  }

  private void processCommand(
      final ItemShopServer server, final ExecutableCommand executableCommand) {
    if (isCommandUnavailable(executableCommand)) {
      logger.debug(
          String.format(
              "Command %s (%s) could not be executed, because %s is Offline.",
              executableCommand.getUniqueId(),
              executableCommand.getCommand(),
              executableCommand.getPlayerName()));

      client.restoreCommand(
          config.shopId,
          server.getServerId(),
          server.getServerToken(),
          executableCommand.getUniqueId());
      return;
    }

    logger.debug(
        String.format(
            "Executing command %s (%s) for %s.",
            executableCommand.getUniqueId(),
            executableCommand.getCommand(),
            executableCommand.getPlayerName()));

    commandDispatcher.dispatchCommand(
        executableCommand.getCommand(), executableCommand.getPlayerName());
  }

  private boolean isCommandUnavailable(final ExecutableCommand command) {
    return command.isRequiresOnline()
        && command.getPlayerName() != null
        && !playerAccessor.isPlayerOnline(command.getPlayerName());
  }
}
