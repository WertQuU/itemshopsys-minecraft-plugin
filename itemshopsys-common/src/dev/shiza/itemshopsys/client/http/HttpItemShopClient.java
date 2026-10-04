package dev.shiza.itemshopsys.client.http;

import dev.shiza.itemshopsys.ItemShopLogger;
import dev.shiza.itemshopsys.client.ItemShopClient;
import dev.shiza.itemshopsys.client.ItemShopClientConfig;
import dev.shiza.itemshopsys.client.command.ExecutableCommands;
import dev.shiza.itemshopsys.platform.PlatformDataProvider;
import java.util.Collections;
import java.util.UUID;
import kong.unirest.Empty;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;

final class HttpItemShopClient implements ItemShopClient {

  private static final ExecutableCommands EMPTY_COMMANDS =
      new ExecutableCommands(Collections.emptyList());
  private final PlatformDataProvider platformDataProvider;
  private final ItemShopLogger logger;

  HttpItemShopClient(final PlatformDataProvider platformDataProvider, final ItemShopLogger logger) {
    this.platformDataProvider = platformDataProvider;
    this.logger = logger;
  }

  void configure(final ItemShopClientConfig config) {
    Unirest.config()
        .defaultBaseUrl(config.apiUrl)
        .addDefaultHeader("Accept", "application/json")
        .addDefaultHeader("User-Agent", "ItemShopSysPlugin/Minecraft")
        .addDefaultHeader("X-PLATFORM", "MinecraftJava")
        .addDefaultHeader("X-VERSION", platformDataProvider.getPluginVersion())
        .addDefaultHeader("X-ENGINE", platformDataProvider.getEngineName())
        .addDefaultHeader("X-ENGINE-VERSION", platformDataProvider.getEngineVersion());
    if (config.debug) {
      Unirest.config().interceptor(new HttpItemShopClientInterceptor(logger));
    }
  }

  @Override
  public ExecutableCommands retrieveCommands(
      final String shopId, final String serverId, final String serverToken) {
    final HttpResponse<ExecutableCommands> response =
        Unirest.get("/plugin/{shopId}/{serverId}/minecraft/commands")
            .routeParam("shopId", shopId)
            .routeParam("serverId", serverId)
            .header("X-SERVER-TOKEN", serverToken)
            .asObject(ExecutableCommands.class);

    if (response.getStatus() == 200) {
      return response.getBody();
    }

    switch (response.getStatus()) {
      case 429:
        logger.debug("Rate limit exceeded, commands could not be retrieved.");
        return EMPTY_COMMANDS;
      case 503:
        logger.debug("Service unavailable, commands could not be retrieved.");
        return EMPTY_COMMANDS;
      case 400:
        logger.debug("Server token is invalid or is empty, commands could not be retrieved.");
        return EMPTY_COMMANDS;
      case 404:
        logger.debug("Invalid shop or server parameters, commands could not be retrieved. Make sure that you have passed them correctly.");
        return EMPTY_COMMANDS;
      default:
        logger.debug("Received unknown HTTP status from ItemShopSys API during commands fetching. Received status code: " + response.getStatus());
        return EMPTY_COMMANDS;
    }
  }

  @Override
  public void restoreCommand(
      final String shopId,
      final String serverId,
      final String serverToken,
      final UUID commandUniqueId) {
    final HttpResponse<Empty> response =
        Unirest.post("/plugin/{shopId}/{serverId}/minecraft/commands/{commandUuid}/restore")
            .routeParam("shopId", shopId)
            .routeParam("serverId", serverId)
            .routeParam("commandUuid", commandUniqueId.toString())
            .header("X-SERVER-TOKEN", serverToken)
            .asEmpty();
    switch (response.getStatus()) {
      case 429:
        logger.debug("Rate limit exceeded, command could not be restored.");
        break;
      case 503:
        logger.debug("Service unavailable, command could not be restored.");
        break;
      default:
        break;
    }
  }
}
