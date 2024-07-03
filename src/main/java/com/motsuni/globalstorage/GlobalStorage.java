package com.motsuni.globalstorage;

import org.bukkit.Server;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GlobalStorage extends JavaPlugin {

    private GlobalInventoryManager globalInventoryManager;

    @Override
    public void onEnable() {
        Server server = getServer();

        // プラグインマネージャー
        PluginManager manager = server.getPluginManager();

        this.globalInventoryManager = new GlobalInventoryManager(this);
        this.globalInventoryManager.init();
    }

    @Override
    public void onDisable() {
        this.globalInventoryManager.save();
    }
}
