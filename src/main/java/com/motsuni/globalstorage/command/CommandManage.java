package com.motsuni.globalstorage.command;

import com.motsuni.globalstorage.GlobalInventoryManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandManage extends SubCommand{
    public CommandManage(GlobalInventoryManager manager, CommandSender commandSender, String @NotNull [] args) {
        super(manager, commandSender, args);
    }

    @Override
    public boolean invoke() {
        switch (this.key) {
            case "backup":
                return backup();
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
}
