package dev.shiza.itemshopsys.bukkit.folia;

public final class FoliaSupport {

  private static final boolean FOLIA = detectFolia();

  private FoliaSupport() {}

  public static boolean isFolia() {
    return FOLIA;
  }

  private static boolean detectFolia() {
    try {
      Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
      return true;
    } catch (final ClassNotFoundException exception) {
      return false;
    }
  }
}
