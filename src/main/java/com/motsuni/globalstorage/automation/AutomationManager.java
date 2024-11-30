package com.motsuni.globalstorage.automation;

// singleton class

import com.motsuni.globalstorage.GlobalInventoryManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AutomationManager {

    private final JavaPlugin plugin;
    private static AutomationManager instance;
    private final GlobalInventoryManager manager;

    private AutoBackup autoBackup = null;
    private Experiment experiment = null;

    private AutomationManager(JavaPlugin plugin, GlobalInventoryManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public static AutomationManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("AutomationManager has not been initialized");
        }

        return instance;
    }

    public static AutomationManager getInstance(JavaPlugin plugin, GlobalInventoryManager manager) {
        if (instance == null) {
            instance = new AutomationManager(plugin, manager);
        }

        return instance;
    }

    public void run() {
        this.reloadAutoBackup();
//        this.reloadExperiment(20);
    }

    public void reloadAutoBackup() {
        if (this.autoBackup != null) {
            this.autoBackup.cancel();
        }
        this.autoBackup = new AutoBackup(this.manager);

        var interval = this.manager.getConfig().getBackupInterval();
        this.autoBackup.runTaskTimer(this.plugin, interval, interval);
    }

    public void reloadExperiment(int interval) {
        if (this.experiment != null) {
            this.experiment.cancel();
        }
        this.experiment = new Experiment();

        this.experiment.runTaskTimer(this.plugin, 0, interval);
    }
}
