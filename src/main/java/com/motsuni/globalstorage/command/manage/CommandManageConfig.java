package com.motsuni.globalstorage.command.manage;

import com.motsuni.globalstorage.GlobalInventoryManager;
import com.motsuni.globalstorage.command.SubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
            case "backup_time":
                return backupTime();
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
        try {
            maxPullAmount = Integer.parseInt(this.args[0]);
        } catch (NumberFormatException e) {
            return false;
        }

        this.manager.getConfig().setMaxPullAmount(maxPullAmount);
        this.manager.getConfig().save();

        return true;
    }

    public boolean backupTime() {
        if (this.args.length == 0) {
            if (this.commandSender instanceof Player) {
                Player player = (Player) this.commandSender;
                player.sendMessage(this.manager.getConfig().getBackupTimes().toString());
            }
            return true;
        }

        if (this.args.length > 1) {
            return false;
        }

        String backupTime = this.args[0];
        this.manager.getConfig().setBackupTimes(List.of(backupTime));
        this.manager.getConfig().save();

        return true;
    }

}
