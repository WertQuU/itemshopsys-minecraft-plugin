package dev.shiza.itemshopsys.client;

import eu.okaeri.configs.OkaeriConfig;
import eu.okaeri.configs.annotation.Comment;
import eu.okaeri.configs.annotation.Exclude;
import eu.okaeri.configs.annotation.Header;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Header("Więcej informacji / More info:")
@Header("https://wiki.itemshopsys.com/pl/plugin")
@Header("")
public final class ItemShopClientConfig extends OkaeriConfig {

  public static final @Exclude String NOT_CONFIGURED_VALUE = "";

  @Comment("tryb debugowania / debug mode")
  public boolean debug = false;

  @Comment("id sklepu / shop id (shop_)")
  public String shopId = NOT_CONFIGURED_VALUE;

  @Comment({
    "lista serwerow (kategorii) - format: id_serwera: token",
    "list of servers (categories) - format: server_id: token",
    "np. / e.g.:",
    "servers:",
    "  serv_aaaa: token1",
    "  serv_bbbb: token2"
  })
  public Map<String, String> servers = new LinkedHashMap<>();

  @Comment("Link do API (zostaw domyslny) / API url (dont change)")
  public String apiUrl = "https://api.itemshopsys.com/v1";

  public List<ItemShopServer> resolveServers() {
    final Map<String, String> resolved = new LinkedHashMap<>();

    if (servers != null) {
      servers.forEach(
          (id, token) -> {
            if (isConfigured(id) && isConfigured(token)) {
              resolved.putIfAbsent(id.trim(), token.trim());
            }
          });
    }

    final List<ItemShopServer> result = new ArrayList<>();
    resolved.forEach((id, token) -> result.add(new ItemShopServer(id, token)));
    return result;
  }

  private static boolean isConfigured(final String value) {
    return value != null && !value.trim().isEmpty();
  }
}
