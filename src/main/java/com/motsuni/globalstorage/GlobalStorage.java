package com.motsuni.globalstorage;

import com.motsuni.globalstorage.automation.AutoBackup;
import com.motsuni.globalstorage.automation.Experiment;
import org.bukkit.plugin.java.JavaPlugin;

public final class GlobalStorage extends JavaPlugin {

    private GlobalInventoryManager globalInventoryManager;

    @Override
    public void onEnable() {
        this.globalInventoryManager = new GlobalInventoryManager(this);
        this.globalInventoryManager.init();

        var interval = this.globalInventoryManager.config.getBackupInterval();

        // 2時間ごとにバックアップを作成
        new AutoBackup(this.globalInventoryManager).runTaskTimer(this, 0, interval);

//        // 1秒ごとに実験
//        new Experiment().runTaskTimer(this, 0, tick);
    }

    @Override
    public void onDisable() {
        this.globalInventoryManager.backup();
    }
}
