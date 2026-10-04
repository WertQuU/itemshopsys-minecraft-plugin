package dev.shiza.itemshopsys.bukkit.folia;

import dev.shiza.itemshopsys.command.CommandDispatcher;
import java.lang.reflect.Method;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * Folia implementation of {@link CommandDispatcher}. Folia requires {@link
 * Server#dispatchCommand} to be called on the global region thread, so every command is
 * scheduled through GlobalRegionScheduler.
 */
public final class FoliaCommandDispatcher implements CommandDispatcher {

  private final Plugin plugin;
  private final Server server;
  private final Object globalScheduler;
  private final Method globalExecute;

  public FoliaCommandDispatcher(final Plugin plugin) {
    this.plugin = plugin;
    this.server = plugin.getServer();
    try {
      this.globalScheduler = Server.class.getMethod("getGlobalRegionScheduler").invoke(server);
      this.globalExecute =
          Class.forName("io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler")
              .getMethod("execute", Plugin.class, Runnable.class);
    } catch (final ReflectiveOperationException exception) {
      throw new IllegalStateException("Could not access Folia GlobalRegionScheduler.", exception);
    }
  }

  @Override
  public void dispatchCommand(final String command) {
    try {
      globalExecute.invoke(
          globalScheduler,
          plugin,
          (Runnable) () -> server.dispatchCommand(server.getConsoleSender(), command));
    } catch (final ReflectiveOperationException exception) {
      plugin
          .getLogger()
          .log(Level.SEVERE, "Could not schedule command on global region thread.", exception);
    }
  }
}
