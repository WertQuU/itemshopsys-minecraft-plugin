plugins {
    id("java")
    id("net.minecrell.plugin-yml.bukkit") version ("0.6.0")
}

dependencies {
    implementation(project(":itemshopsys-common"))
    implementation("eu.okaeri:okaeri-configs-yaml-bukkit:5.0.5")
    compileOnly("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
}

bukkit {
    main = "dev.shiza.itemshopsys.bukkit.ItemShopBukkitPlugin"
    name = "itemshopsys"
    author = "shiza"
    version = project.version.toString()
    apiVersion = "1.13"
    foliaSupported = true
    commands {
        register("itemshopsys") {
            permission = "itemshopsys.reload"
        }
    }
}