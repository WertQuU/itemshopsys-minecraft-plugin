package dev.shiza.itemshopsys;

import static dev.shiza.itemshopsys.client.ItemShopClientConfig.NOT_CONFIGURED_VALUE;

import dev.shiza.itemshopsys.client.ItemShopClient;
import dev.shiza.itemshopsys.client.ItemShopClientConfig;
import dev.shiza.itemshopsys.client.http.HttpItemShopClientFactory;
import dev.shiza.itemshopsys.command.CommandDispatcher;
import dev.shiza.itemshopsys.command.CommandPoolTask;
import dev.shiza.itemshopsys.config.ConfigFactory;
import dev.shiza.itemshopsys.platform.PlatformDataProvider;
import dev.shiza.itemshopsys.player.PlayerAccessor;
import dev.shiza.itemshopsys.scheduler.TaskScheduler;
import java.time.Duration;

final class ItemShopPluginImpl implements ItemShopPlugin {

  private final PlatformDataProvider platformDataProvider;
  private final ItemShopLogger logger;
  private final ConfigFactory configFactory;
  private final TaskScheduler taskScheduler;
  private final PlayerAccessor playerAccessor;
  private final CommandDispatcher commandDispatcher;
  private ItemShopClientConfig config;

  ItemShopPluginImpl(
      final PlatformDataProvider platformDataProvider,
      final ItemShopLogger logger,
      final ConfigFactory configFactory,
      final TaskScheduler taskScheduler,
      final PlayerAccessor playerAccessor,
      final CommandDispatcher commandDispatcher) {
    this.platformDataProvider = platformDataProvider;
    this.logger = logger;
    this.configFactory = configFactory;
    this.taskScheduler = taskScheduler;
    this.playerAccessor = playerAccessor;
    this.commandDispatcher = commandDispatcher;
  }

  @Override
  public void onEnable() {
    config = configFactory.produceConfig(ItemShopClientConfig.class, "config.yml");
    logger.setDebugEnabled(config.debug);

    if (requiresConfiguration()) {
      logger.error("You need to configure the plugin before using it.");
      return;
    }

    final ItemShopClient client =
        HttpItemShopClientFactory.create(platformDataProvider, logger, config);

    taskScheduler.schedule(
        new CommandPoolTask(logger, config, client, playerAccessor, commandDispatcher),
        Duration.ofMinutes(1));
  }

  @Override
  public ItemShopClientConfig getConfig() {
    return config;
  }

  public boolean requiresConfiguration() {
    return config.shopId.equals(NOT_CONFIGURED_VALUE) || config.resolveServers().isEmpty();
  }
}
