package com.motsuni.globalstorage;

import com.motsuni.globalstorage.automation.AutomationManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class GlobalStorage extends JavaPlugin {

    private GlobalInventoryManager globalInventoryManager;

    @Override
    public void onEnable() {
        this.globalInventoryManager = new GlobalInventoryManager(this);
        this.globalInventoryManager.init();

        AutomationManager automationManager = AutomationManager.getInstance(this, this.globalInventoryManager);
        automationManager.run();
    }

    @Override
    public void onDisable() {
        this.globalInventoryManager.backup();
    }
}
