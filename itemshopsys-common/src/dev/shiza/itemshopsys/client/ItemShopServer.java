package dev.shiza.itemshopsys.client;

public final class ItemShopServer {

  private final String serverId;
  private final String serverToken;

  public ItemShopServer(final String serverId, final String serverToken) {
    this.serverId = serverId;
    this.serverToken = serverToken;
  }

  public String getServerId() {
    return serverId;
  }

  public String getServerToken() {
    return serverToken;
  }
}
