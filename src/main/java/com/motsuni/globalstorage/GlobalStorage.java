package com.motsuni.globalstorage;

import com.motsuni.globalstorage.command.CommandGi;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class GlobalStorage extends JavaPlugin {

    private GlobalInventoryManager globalInventoryManager;

    @Override
    public void onEnable() {
        this.globalInventoryManager = new GlobalInventoryManager(this);
        this.globalInventoryManager.init();

        PluginCommand gi = this.getCommand("gi");
        if (gi != null) {
            gi.setExecutor(new CommandGi());
        }
    }

    @Override
    public void onDisable() {
         this.globalInventoryManager.save();
    }
}
