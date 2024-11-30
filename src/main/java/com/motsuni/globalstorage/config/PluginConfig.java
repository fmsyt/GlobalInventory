package com.motsuni.globalstorage.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PluginConfig {
    protected Plugin plugin;
    protected FileConfiguration config;


    public PluginConfig(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        // NOTE: プレイヤーが保持できるアイテムの最大数を初期値に設定
        this.config.addDefault(EnumConfigKeys.MAX_PULL_AMOUNT.getKey(), 64 * 9 * 5);

        // バックアップを作成する時間の初期値を設定
        this.config.addDefault(EnumConfigKeys.BACKUP_TIMES.getKey(), List.of("0:00"));

        this.save();
    }

    public FileConfiguration getConfig() {
        return this.config;
    }

    public void save() {
        this.plugin.saveConfig();
    }

    /**
     * pullコマンドで1度に引き出せるアイテムの最大数を取得
     * @see com.motsuni.globalstorage.command.Command
     */
    public int getMaxPullAmount() {
        return this.config.getInt(EnumConfigKeys.MAX_PULL_AMOUNT.getKey());
    }

    /**
     * pullコマンドで1度に引き出せるアイテムの最大数を設定
     * @see com.motsuni.globalstorage.command.Command
     * @throws IllegalArgumentException 引数が負の場合
     */
    public void setMaxPullAmount(int maxPullAmount) {
        if (maxPullAmount < 0) {
            throw new IllegalArgumentException("Invalid max pull amount: " + maxPullAmount);
        }

        this.config.set(EnumConfigKeys.MAX_PULL_AMOUNT.getKey(), maxPullAmount);
    }

    /**
     * バックアップを作成する時間を取得
     * @see com.motsuni.globalstorage.command.CommandManage
     */
    public List<String> getBackupTimes() {
        return this.config.getStringList(EnumConfigKeys.BACKUP_TIMES.getKey());
    }

    /**
     * バックアップを作成する時間を設定
     * @see com.motsuni.globalstorage.command.CommandManage
     * @throws IllegalArgumentException 引数がH:mm:ss形式でない場合
     */
    public void setBackupTimes(@NotNull List<String> backupTimes) {
        // check format if it is H:mm:ss
        for (String time : backupTimes) {
            if (!time.matches("^([01]?[0-9]|2[0-3]):[0-5][0-9]$")) {
                throw new IllegalArgumentException("Invalid time format: " + time);
            }
        }

        this.config.set(EnumConfigKeys.BACKUP_TIMES.getKey(), backupTimes);
    }
}
