package com.motsuni.globalstorage;

import org.bukkit.plugin.java.JavaPlugin;

public final class GlobalStorage extends JavaPlugin {

    private GlobalInventoryManager globalInventoryManager;

    @Override
    public void onEnable() {
        this.globalInventoryManager = new GlobalInventoryManager(this);
        this.globalInventoryManager.init();
    }

    @Override
    public void onDisable() {
         this.globalInventoryManager.save();
    }
}
