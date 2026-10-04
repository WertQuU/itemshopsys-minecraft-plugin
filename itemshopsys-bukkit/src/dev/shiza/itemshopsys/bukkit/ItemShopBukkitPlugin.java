package dev.shiza.itemshopsys.bukkit;

import dev.shiza.itemshopsys.ItemShopPlugin;
import dev.shiza.itemshopsys.bukkit.command.BukkitCommandDispatcher;
import dev.shiza.itemshopsys.bukkit.command.BukkitItemShopCommand;
import dev.shiza.itemshopsys.bukkit.folia.FoliaCommandDispatcher;
import dev.shiza.itemshopsys.bukkit.folia.FoliaSupport;
import dev.shiza.itemshopsys.bukkit.folia.FoliaTaskScheduler;
import dev.shiza.itemshopsys.bukkit.player.BukkitPlayerAccessor;
import dev.shiza.itemshopsys.bukkit.scheduler.BukkitTaskScheduler;
import dev.shiza.itemshopsys.command.CommandDispatcher;
import dev.shiza.itemshopsys.config.ConfigFactory;
import dev.shiza.itemshopsys.platform.PlatformDataProvider;
import dev.shiza.itemshopsys.scheduler.TaskScheduler;
import eu.okaeri.configs.yaml.bukkit.YamlBukkitConfigurer;
import java.util.Objects;
import org.bukkit.plugin.java.JavaPlugin;

public final class ItemShopBukkitPlugin extends JavaPlugin implements PlatformDataProvider {

  private ItemShopPlugin itemShopPlugin;

  @Override
  public void onEnable() {
    final boolean folia = FoliaSupport.isFolia();
    final TaskScheduler taskScheduler =
        folia ? new FoliaTaskScheduler(this) : new BukkitTaskScheduler(this);
    final CommandDispatcher commandDispatcher =
        folia ? new FoliaCommandDispatcher(this) : new BukkitCommandDispatcher(this);

    itemShopPlugin =
        ItemShopPlugin.create(
            this,
            new ItemShopBukkitLogger(getServer()),
            new ConfigFactory(getDataFolder().toPath(), YamlBukkitConfigurer::new),
            taskScheduler,
            new BukkitPlayerAccessor(getServer()),
            commandDispatcher);
    itemShopPlugin.onEnable();

    Objects.requireNonNull(getCommand("itemshopsys"))
        .setExecutor(new BukkitItemShopCommand(itemShopPlugin.getConfig()));
  }

  @Override
  public void onDisable() {
    if (itemShopPlugin != null) {
      itemShopPlugin.onDisable();
    }
  }

  @Override
  public String getPluginVersion() {
    return getDescription().getVersion();
  }

  @Override
  public String getEngineName() {
    return "Bukkit";
  }

  @Override
  public String getEngineVersion() {
    return PlatformDataProvider.matchVersion(getServer().getVersion());
  }
}
