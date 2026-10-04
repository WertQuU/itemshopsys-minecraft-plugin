package dev.shiza.itemshopsys.bukkit.folia;

import dev.shiza.itemshopsys.scheduler.TaskScheduler;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

/**
 * Folia implementation of {@link TaskScheduler}. Uses AsyncScheduler through reflection, so the
 * plugin can still be compiled against the old Spigot API (Java 8).
 */
public final class FoliaTaskScheduler implements TaskScheduler {

  private final Plugin plugin;
  private final Object asyncScheduler;
  private final Method runAtFixedRate;

  public FoliaTaskScheduler(final Plugin plugin) {
    this.plugin = plugin;
    try {
      final Server server = plugin.getServer();
      this.asyncScheduler = Server.class.getMethod("getAsyncScheduler").invoke(server);
      this.runAtFixedRate =
          Class.forName("io.papermc.paper.threadedregions.scheduler.AsyncScheduler")
              .getMethod(
                  "runAtFixedRate",
                  Plugin.class,
                  Consumer.class,
                  long.class,
                  long.class,
                  TimeUnit.class);
    } catch (final ReflectiveOperationException exception) {
      throw new IllegalStateException("Could not access Folia AsyncScheduler.", exception);
    }
  }

  @Override
  public void schedule(final Runnable task, final Duration delay) {
    final long millis = Math.max(1L, delay.toMillis());
    final Consumer<Object> wrapped =
        scheduledTask -> {
          try {
            task.run();
          } catch (final Throwable throwable) {
            plugin.getLogger().log(Level.SEVERE, "Task execution failed.", throwable);
          }
        };

    try {
      runAtFixedRate.invoke(
          asyncScheduler, plugin, wrapped, millis, millis, TimeUnit.MILLISECONDS);
    } catch (final ReflectiveOperationException exception) {
      throw new IllegalStateException("Could not schedule task on Folia.", exception);
    }
  }
}
