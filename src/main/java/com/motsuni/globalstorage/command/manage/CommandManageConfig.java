package com.motsuni.globalstorage.command.manage;

import com.motsuni.globalstorage.GlobalInventoryManager;
import com.motsuni.globalstorage.command.SubCommand;
import com.motsuni.globalstorage.config.PluginConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandManageConfig extends SubCommand {
    public CommandManageConfig(GlobalInventoryManager manager, CommandSender commandSender, String @NotNull [] args) {
        super(manager, commandSender, args);
    }

    @Override
    public boolean invoke() {
        if (!this.commandSender.isOp()) {
            return false;
        }

        switch (this.key) {
            case "max_pull_amount":
                return maxPullAmount();
            case "backup_interval":
                return backupInterval();
            default:
                return false;
        }
    }

    public boolean maxPullAmount() {
        if (this.args.length == 0) {
            if (this.commandSender instanceof Player) {
                Player player = (Player) this.commandSender;
                player.sendMessage(String.format("%d 個", this.manager.getConfig().getMaxPullAmount()));
            }
            return true;
        }

        if (this.args.length > 1) {
            return false;
        }

        int maxPullAmount;

        if (this.args[0].equals("default")) {
            maxPullAmount = PluginConfig.getDefaultMaxPullAmount();
        } else {
            try {
                maxPullAmount = Integer.parseInt(this.args[0]);
            } catch (NumberFormatException e) {
                return false;
            }
        }

        this.manager.getConfig().setMaxPullAmount(maxPullAmount);
        this.manager.getConfig().save();

        sendMessageToOperator("引き出し可能な最大数が変更されました: " + maxPullAmount);

        return true;
    }

    public boolean backupInterval() {
        if (this.args.length == 0) {
            if (this.commandSender instanceof Player) {
                Player player = (Player) this.commandSender;
                var interval = this.manager.getConfig().getBackupInterval();

                int tick = 20;
                int sec = interval / tick;
                int min = sec / 60;
                int hour = min / 60;

                player.sendMessage(String.format("%d (interval H:mm:ss: %d:%02d:%02d)", interval, hour, min % 60, sec % 60));
            }
            return true;
        }

        if (this.args.length > 1) {
            return false;
        }

        int interval;

        if (this.args[0].equals("default")) {
            interval = PluginConfig.getDefaultBackupInterval();
        } else {
            try {
                interval = Integer.parseInt(this.args[0]);
                if (interval < 20) {
                    if (this.commandSender instanceof Player) {
                        Player player = (Player) this.commandSender;
                        player.sendMessage("バックアップの間隔は20(tick)以上で設定してください");
                    }

                    return true;
                }

            } catch (NumberFormatException e) {
                return false;
            }
        }

        this.manager.getConfig().setBackupInterval(interval);
        this.manager.getConfig().save();

        sendMessageToOperator("バックアップの間隔が変更されました: " + tickToString(interval));

        return true;
    }

    private String tickToString(int tick) {
        int sec = tick / 20;
        int min = sec / 60;
        int hour = min / 60;

        return String.format("%d:%02d:%02d", hour, min % 60, sec % 60);
    }

    private void sendMessageToOperator(String message) {
        this.manager.getPlugin().getServer().getOperators().forEach(op -> {
            if (!op.isOnline()) {
                return;
            }

            Player player = op.getPlayer();
            if (player == null) {
                return;
            }

            player.sendMessage(message);
        });
    }
}
