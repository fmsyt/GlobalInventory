package com.motsuni.globalstorage.automation;

import com.motsuni.globalstorage.GlobalInventoryManager;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class AutoBackup extends BukkitRunnable {

    private final GlobalInventoryManager manager;

    /**
     * バックアップ完了時に通知するかどうか
     */
    public boolean notify = false;

    public AutoBackup(GlobalInventoryManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        String filename = this.manager.backup();

        if (!this.notify) {
            return;
        }

        this.manager.getPlugin().getServer().getOperators().forEach(op -> {
            if (!op.isOnline()) {
                return;
            }

            Player player = op.getPlayer();
            if (player == null) {
                return;
            }

            player.sendMessage("バックアップを作成しました: " + filename);
        });
    }
}
