package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import com.motsuni.globalstorage.command.manage.CommandManageConfig;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandManage extends SubCommand {
    public CommandManage(GlobalInventoryManager manager, CommandSender commandSender, String @NotNull [] args) {
        super(manager, commandSender, args);
    }

    @Override
    public boolean invoke() {

        if (!this.commandSender.isOp()) {
            return false;
        }

        switch (this.key) {
            case "backup":
                return backup();
            case "config":
                return config();
            default:
                return false;
        }
    }

    public boolean backup() {
        String filename = this.manager.backup();
        if (filename != null && this.commandSender instanceof Player) {
            Player player = (Player) this.commandSender;
            player.sendMessage("バックアップを作成しました: " + filename);
        }

        return true;
    }

    public boolean config() {
        return new CommandManageConfig(this.manager, this.commandSender, this.args).invoke();
    }
}
