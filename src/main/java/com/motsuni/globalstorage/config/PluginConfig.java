package com.motsuni.globalstorage.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PluginConfig {
    protected Plugin plugin;
    protected FileConfiguration config;


    public PluginConfig(@NotNull Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();

        // NOTE: プレイヤーが保持できるアイテムの最大数を初期値に設定
        this.config.addDefault(EnumConfigKeys.MAX_PULL_AMOUNT.getKey(), getDefaultMaxPullAmount());

        // NOTE: バックアップを作成する時間を初期値に設定
        this.config.addDefault(EnumConfigKeys.BACKUP_INTERVAL.getKey(), getDefaultBackupInterval());

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

    public static int getDefaultMaxPullAmount() {
        return 64 * 9 * 5;
    }

    /**
     * バックアップを作成する時間を取得
     * @see com.motsuni.globalstorage.command.CommandManage
     */
    public int getBackupInterval() {
        return this.config.getInt(EnumConfigKeys.BACKUP_INTERVAL.getKey());
    }

    public static int getDefaultBackupInterval() {
        return 20 * 60 * 60 * 2;
    }

    /**
     * バックアップを作成する時間を設定
     * @see com.motsuni.globalstorage.command.CommandManage
     * @throws IllegalArgumentException 引数がH:mm:ss形式でない場合
     */
    public void setBackupInterval(int tick) {
        if (tick < 0) {
            throw new IllegalArgumentException("Invalid backup interval: " + tick);
        }
        this.config.set(EnumConfigKeys.BACKUP_INTERVAL.getKey(), tick);
    }
}
